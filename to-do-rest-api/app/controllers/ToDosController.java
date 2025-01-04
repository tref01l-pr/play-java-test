package controllers;

import Contracts.Requests.CreateToDoRequest;
import Contracts.Requests.FileMetadataRequest;
import Contracts.Requests.UpdateToDoRequest;
import Contracts.Responses.ToDoResponse;
import com.google.inject.Inject;
import com.mongodb.client.ClientSession;
import entities.mongodb.MongoDbToDo;
import jwt.JwtControllerHelper;
import models.FileMetadata;
import models.ToDo;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import services.MongoDb;
import store.FilesStore;
import store.ToDosStore;
import play.mvc.Result;
import play.mvc.Results;
import store.UsersStore;

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
    private final FilesStore filesStore;


    @Inject
    public ToDosController(HttpExecutionContext ec, ToDosStore toDosStore, UsersStore userStore, FilesStore filesStore) {
        this.ec = ec;
        this.toDosStore = toDosStore;
        this.usersStore = userStore;
        this.filesStore = filesStore;
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
                    FileMetadata metadata = filesStore.create(filePart);
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

            UpdateToDoRequest updateToDoRequest = Json.fromJson(Json.parse(jsonPart), UpdateToDoRequest.class);
            MongoDbToDo toDoExist = toDosStore.getById(updateToDoRequest.getId());

            if (toDoExist == null) {
                return Results.notFound("To-Do not found");
            }

            if (!toDoExist.getUserId().equals(userId)) {
                return Results.forbidden("To-Do does not belong to user");
            }

            List<Http.MultipartFormData.FilePart<File>> fileParts = body.getFiles();
            List<FileMetadata> uploadedFiles = new ArrayList<>();

            if (hasFileHashChanged(updateToDoRequest.getFilesMetadata(), toDoExist.getFiles())) {
                return Results.badRequest("Files can't be added with request");
            }

            var filesToDelete = getFilesToDelete(updateToDoRequest.getFilesMetadata(), toDoExist.getFiles());

            try {
                for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
                    FileMetadata metadata = filesStore.create(filePart);
                    uploadedFiles.add(metadata);
                }

                for (FileMetadata file : filesToDelete) {
                    filesStore.removeByFileMetadata(file);
                }

                List<FileMetadata> unchangedFiles = toDoExist.getFiles().stream()
                        .filter(existingFile -> updateToDoRequest.getFilesMetadata().stream()
                                .anyMatch(requestFile -> requestFile.getHash().equals(existingFile.getHash())))
                        .collect(Collectors.toList());

                uploadedFiles.addAll(unchangedFiles);

                ToDo toDo = ToDo.create(userId, updateToDoRequest.getTitle(), updateToDoRequest.getDescription(), updateToDoRequest.getTags(), uploadedFiles);
                toDo.setId(updateToDoRequest.getId());

                MongoDbToDo upd = toDosStore.update(toDo);
                MongoDbToDo updatedToDo = toDosStore.getById(updateToDoRequest.getId());
                ToDoResponse response = new ToDoResponse(updatedToDo.getId(), updatedToDo.getTitle(), updatedToDo.getDescription(), updatedToDo.getCreatedAt(), updatedToDo.getTags(), uploadedFiles);
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
                toDo.getFiles().forEach(file -> filesStore.removeByFileMetadata(file));
                toDosStore.removeById(toDoObjectId);
                return Results.ok("Successfully removed To-Do");
            } catch (Exception e) {
                return Results.internalServerError("Error removing To-Do");
            }
        });
    }

    public Result exportToDoById(Http.Request request, String toDoId) {
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
                File zipFile = filesStore.exportFileWithToDo(toDo);

                return Results.ok(zipFile)
                        .as("application/zip")
                        .withHeader("Content-Disposition", "attachment; filename=" + zipFile.getName())
                        .withHeader("Access-Control-Expose-Headers", "Content-Disposition");
            } catch (IllegalArgumentException e) {
                return Results.notFound("ToDo not found: " + e.getMessage());
            } catch (Exception e) {
                return Results.internalServerError("Error during export: " + e.getMessage());
            }
        });
    }

    private boolean hasFileHashChanged(List<FileMetadataRequest> filesFromRequest, List<FileMetadata> filesFromDatabase) {
        return filesFromRequest.stream()
                .anyMatch(f -> filesFromDatabase.stream()
                        .noneMatch(tde -> tde.getHash().equals(f.getHash())));
    }

    private List<FileMetadata> getFilesToDelete(List<FileMetadataRequest> filesFromRequest, List<FileMetadata> filesFromDatabase) {
        return filesFromDatabase.stream()
                .filter(fileInDb -> filesFromRequest.stream()
                        .noneMatch(fileInRequest -> fileInRequest.getHash().equals(fileInDb.getHash())))
                .collect(Collectors.toList());
    }
}
