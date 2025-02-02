package services;

import Contracts.Requests.CreateToDoRequest;
import Contracts.Requests.UpdateToDoRequest;
import Contracts.Responses.ToDoResponse;
import CustomExceptions.*;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import models.ToDo;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import store.ToDosStore;
import store.UsersStore;

import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ToDosService {
    private final ToDosStore toDosStore;
    private final UsersStore usersStore;
    private final FilesService filesService;
    private final Validator validator;

    @Inject
    public ToDosService(ToDosStore toDosStore, UsersStore usersStore, FilesService filesService,
                        Validator validator) {
        this.toDosStore = toDosStore;
        this.usersStore = usersStore;
        this.filesService = filesService;
        this.validator = validator;
    }

    public List<ToDoResponse> getAllToDos() {
        List<? extends MongoDbToDo> toDos = toDosStore.getAll();
        return toDos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public List<ToDoResponse> getToDosByUser(String username, ObjectId userId) {
        try {
            var user = usersStore.getByUsername(username);
            if (user == null || !user.getId().equals(userId)) {
                throw new ResourceNotFoundException("User not found");
            }

            List<MongoDbToDo> toDos = toDosStore.getByUserId(userId);
            return toDos.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            Logger.error("Invalid user ID format: " + userId, e);
            throw new InvalidRequestException("Invalid user ID format");
        }
    }

    public ToDoResponse getToDoById(String toDoId, ObjectId userId) {
        try {
            MongoDbToDo toDo = toDosStore.getById(new ObjectId(toDoId));
            if (toDo == null || !toDo.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("To-Do not found");
            }
            return mapToResponse(toDo);
        } catch (IllegalArgumentException e) {
            Logger.error("Invalid todo ID format: " + toDoId, e);
            throw new InvalidRequestException("Invalid todo ID format");
        }
    }

    public ToDoResponse createToDo(Http.Request request, ObjectId userId) {
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();
        List<FileMetadata> uploadedFiles = Collections.emptyList();

        if (body == null) {
            throw new InvalidRequestException("Request must be multipart/form-data");
        }

        String jsonPart = getJsonPart(body);
        if (jsonPart == null) {
            throw new InvalidRequestException("Missing JSON part of the request");
        }

        var createToDoRequest = Json.fromJson(Json.parse(jsonPart), CreateToDoRequest.class);
        validateRequest(createToDoRequest);

        if (!createToDoRequest.getUserId().equals(userId)) {
            throw new InvalidRequestException("User ID in token does not match user ID in request");
        }

        uploadedFiles = filesService.create(body.getFiles());

        ToDo toDo = ToDo.create(userId, createToDoRequest.getTitle(),
                createToDoRequest.getDescription(), createToDoRequest.getTags(), uploadedFiles);

        MongoDbToDo createdToDo = toDosStore.create(toDo);
        Logger.info("To-Do created: " + createdToDo.getId());

        return mapToResponse(createdToDo);
    }

    public ToDoResponse updateToDo(Http.Request request, ObjectId userId) {
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();
        List<FileMetadata> uploadedFiles = Collections.emptyList();

        if (body == null) {
            throw new InvalidRequestException("Request must be multipart/form-data");
        }

        String jsonPart = getJsonPart(body);
        if (jsonPart == null) {
            throw new InvalidRequestException("Missing JSON part of the request");
        }

        var updateToDoRequest = Json.fromJson(Json.parse(jsonPart), UpdateToDoRequest.class);
        validateRequest(updateToDoRequest);

        MongoDbToDo existingToDo = toDosStore.getById(updateToDoRequest.getId());
        if (existingToDo == null || !existingToDo.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("To-Do not found or does not belong to user");
        }

        if (filesService.hasFileHashChanged(updateToDoRequest.getFilesMetadata(), existingToDo.getFiles())) {
            throw new InvalidRequestException("Files can't be added with request");
        }

        List<FileMetadata> filesToDelete = filesService.getFilesToDelete(
                updateToDoRequest.getFilesMetadata(), existingToDo.getFiles());
        filesService.deleteFiles(filesToDelete);

        uploadedFiles = filesService.uploadFiles(body.getFiles());
        List<FileMetadata> unchangedFiles = filesService.mergeFiles(
                updateToDoRequest.getFilesMetadata(), existingToDo.getFiles());
        uploadedFiles.addAll(unchangedFiles);

        ToDo updatedToDo = ToDo.create(userId, updateToDoRequest.getTitle(),
                updateToDoRequest.getDescription(), updateToDoRequest.getTags(), uploadedFiles);
        updatedToDo.setId(updateToDoRequest.getId());

        MongoDbToDo result = toDosStore.update(updatedToDo);
        Logger.info("To-Do updated: " + result.getId());

        return mapToResponse(result);
    }

    public void deleteToDoById(String toDoId, ObjectId userId) {
        try {
            MongoDbToDo toDo = toDosStore.getById(new ObjectId(toDoId));
            if (toDo == null || !toDo.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("To-Do not found or does not belong to user");
            }

            filesService.deleteFiles(toDo.getFiles());

            toDosStore.removeById(new ObjectId(toDoId));
            Logger.info("To-Do was deleted: " + toDoId);
        } catch (IllegalArgumentException e) {
            Logger.error("Invalid todo ID format: " + toDoId, e);
            throw new InvalidRequestException("Invalid todo ID format");
        }
    }

    public File exportToDoById(String toDoId, ObjectId userId) {
        try {
            MongoDbToDo toDo = toDosStore.getById(new ObjectId(toDoId));
            if (toDo == null || !toDo.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("To-Do not found or does not belong to user");
            }

            File zipFile = filesService.exportToDoFiles(toDo);
            Logger.info("To-Do was exported: " + toDoId);
            return zipFile;
        } catch (IllegalArgumentException e) {
            Logger.error("Invalid todo ID format: " + toDoId, e);
            throw new InvalidRequestException("Invalid todo ID format");
        }
    }

    private void validateRequest(Object request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new InvalidRequestException(
                    violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .collect(Collectors.joining(", "))
            );
        }
    }

    private String getJsonPart(Http.MultipartFormData<File> body) {
        String[] jsonParts = body.asFormUrlEncoded().get("data");
        return (jsonParts != null && jsonParts.length > 0) ? jsonParts[0] : null;
    }

    private ToDoResponse mapToResponse(MongoDbToDo toDo) {
        return new ToDoResponse(
                toDo.getId(),
                toDo.getTitle(),
                toDo.getDescription(),
                toDo.getCreatedAt(),
                toDo.getTags(),
                toDo.getFiles()
        );
    }
}