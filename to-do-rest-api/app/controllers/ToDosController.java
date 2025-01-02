package controllers;

import Contracts.Requests.CreateToDoRequest;
import Contracts.Requests.UpdateToDoRequest;
import Contracts.Responses.ToDoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.client.gridfs.GridFSBucket;
import entities.mongodb.MongoDbToDo;
import jwt.JwtControllerHelper;
import jwt.VerifiedJwt;
import models.FileMetadata;
import models.ToDo;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import services.MongoDb;
import store.ToDosStore;
import play.mvc.Result;
import play.mvc.Results;
import store.UsersStore;
import utils.PdfUtils;
import play.libs.Files.TemporaryFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class ToDosController {
    private final HttpExecutionContext ec;
    private final ToDosStore toDosStore;
    private final UsersStore usersStore;


    @Inject
    public ToDosController(HttpExecutionContext ec, ToDosStore toDosStore, UsersStore userStore) {
        this.ec = ec;
        this.toDosStore = toDosStore;
        this.usersStore = userStore;
    }

    @Inject
    private MongoDb mongoDb;

    @Inject
    private JwtControllerHelper jwtControllerHelper;

    public CompletionStage<Result> listToDos(Http.Request request) {
        return supplyAsync(() -> {
            List<? extends MongoDbToDo> toDos = toDosStore.getAll();
            List<ToDoResponse> response = toDos.stream()
                    .map(toDo -> new ToDoResponse(
                            toDo.getId(),
                            toDo.getTitle(),
                            toDo.getDescription(),
                            toDo.getCreatedAt(),
                            toDo.getTags(),
                            toDo.getFiles()
                    ))
                    .collect(Collectors.toList());
            return Results.ok(Json.toJson(response));
        }, ec.current());
    }

    public Result listToDosByUser(Http.Request request, String username) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var userExist = usersStore.getByUsername(username);
            if (userExist == null) {
                return Results.notFound("User not found");
            }

            if (!userExist.getId().equals(userId)) {
                return Results.forbidden("User ID in token does not match user ID in request");
            }

            List<MongoDbToDo> toDos = toDosStore.getByUserId(userId);
            List<ToDoResponse> response = toDos.stream()
                    .map(toDo -> new ToDoResponse(
                            toDo.getId(),
                            toDo.getTitle(),
                            toDo.getDescription(),
                            toDo.getCreatedAt(),
                            toDo.getTags(),
                            toDo.getFiles()
                    ))
                    .collect(Collectors.toList());

            return Results.ok(Json.toJson(response));
        });
    }

    public Result getToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var toDoObjectId = new org.bson.types.ObjectId(toDoId);
            MongoDbToDo toDo = toDosStore.getById(toDoObjectId);

            if (toDo == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDo.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            ToDoResponse response = new ToDoResponse(toDo.getId(), toDo.getTitle(), toDo.getDescription(), toDo.getCreatedAt(), toDo.getTags(), toDo.getFiles());
            return Results.ok(Json.toJson(response));
        });
    }

    public Result createToDo(Http.Request request) {
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            if (body == null) {
                return Results.badRequest("Request must be multipart/form-data");
            }

            String[] jsonParts = body.asFormUrlEncoded().get("data");
            if (jsonParts == null || jsonParts.length == 0) {
                return Results.badRequest("Missing JSON part of the request");
            }

            String jsonPart = jsonParts[0];
            if (jsonPart == null) {
                return Results.badRequest("Missing JSON part of the request");
            }

            CreateToDoRequest createToDoRequest = Json.fromJson(Json.parse(jsonPart), CreateToDoRequest.class);

            if (createToDoRequest.getTitle() == null || createToDoRequest.getDescription() == null) {
                return Results.badRequest("Missing title or description");
            }

            if (!createToDoRequest.getUserId().equals(userId)) {
                return Results.forbidden("User ID in token does not match user ID in request");
            }

            List<Http.MultipartFormData.FilePart<File>> fileParts = body.getFiles();
            List<FileMetadata> uploadedFiles = new ArrayList<>();

            try {
                for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
                    String fileName = filePart.getFilename();
                    String contentType = filePart.getContentType();
                    FileMetadata metadata = new FileMetadata();

                    metadata.setFileName(fileName);
                    metadata.setFileType(contentType);

                    TemporaryFile tempFile = (TemporaryFile) filePart.getRef();
                    File file = tempFile.path().toFile();
                    try (InputStream fileStream = new BufferedInputStream(new FileInputStream(file))) {
                        fileStream.mark(Integer.MAX_VALUE);
                        String pdfHash = PdfUtils.calculatePdfHash(fileStream);
                        metadata.setPdfHash(pdfHash);
                        fileStream.reset();

                        List<ObjectId> imageIds = new ArrayList<>();
                        RandomAccessRead randomAccessRead = new RandomAccessReadBuffer(fileStream);

                        try (PDDocument document = Loader.loadPDF(randomAccessRead)) {
                            PDFRenderer pdfRenderer = new PDFRenderer(document);

                            for (int page = 0; page < document.getNumberOfPages(); page++) {
                                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(bim, "png", baos);
                                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                                ObjectId imageId = mongoDb.getGridFSBucket().uploadFromStream("page-" + page + ".png", bais);
                                imageIds.add(imageId);
                            }
                        }
                        metadata.setImageIds(imageIds);


                    }
                    uploadedFiles.add(metadata);
                }

                ToDo toDo = ToDo.create(createToDoRequest.getUserId(), createToDoRequest.getTitle(), createToDoRequest.getDescription(), createToDoRequest.getTags(), uploadedFiles);
                MongoDbToDo createdToDo = toDosStore.create(toDo);
                ToDoResponse response = new ToDoResponse(createdToDo.getId(), createdToDo.getTitle(), createdToDo.getDescription(), createdToDo.getCreatedAt(), createdToDo.getTags(), createdToDo.getFiles());
                return Results.created(Json.toJson(response));
            }
            catch (Exception e) {
                return Results.internalServerError(e.getMessage());
            }
        });
    }

    public Result updateToDo(Http.Request request) {
        JsonNode json = request.body().asJson();
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            if (json == null) {
                return Results.badRequest("Invalid JSON data");
            }

            UpdateToDoRequest updateToDoRequest = Json.fromJson(json, UpdateToDoRequest.class);

            MongoDbToDo toDoExist = toDosStore.getById(updateToDoRequest.getId());

            if (toDoExist == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDoExist.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            try {
                //TODO remove null and make normal logic
                ToDo toDo = ToDo.create(userId, updateToDoRequest.getTitle(), updateToDoRequest.getDescription(), updateToDoRequest.getTags(), null);
                toDo.setId(updateToDoRequest.getId());
                MongoDbToDo updatedToDo = toDosStore.update(toDo);
                ToDoResponse response = new ToDoResponse(updatedToDo.getId(), updatedToDo.getTitle(), updatedToDo.getDescription(), updatedToDo.getCreatedAt(), updatedToDo.getTags(), null);
                return Results.ok(Json.toJson(response));
            }
            catch (Exception e) {
                return Results.internalServerError(e.getMessage());
            }


        });
    }

    public Result deleteToDoById(Http.Request request, String toDoId) {
        return jwtControllerHelper.withAuthenticatedUser(request, userId -> {
            var toDoObjectId = new org.bson.types.ObjectId(toDoId);
            MongoDbToDo toDo = toDosStore.getById(toDoObjectId);
            if (toDo == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDo.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            try {
                toDosStore.removeById(toDoObjectId);
                return Results.noContent();
            } catch (Exception e) {
                return Results.internalServerError("Error removing To-Do");
            }
        });
    }
}
