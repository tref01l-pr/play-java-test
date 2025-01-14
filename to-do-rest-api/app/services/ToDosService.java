package services;

import Contracts.Requests.CreateToDoRequest;
import Contracts.Requests.UpdateToDoRequest;
import Contracts.Responses.ToDoResponse;
import com.google.inject.Inject;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import models.ToDo;
import org.bson.types.ObjectId;
import play.mvc.Http;
import store.ToDosStore;
import store.UsersStore;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ToDosService {
    private final ToDosStore toDosStore;
    private final UsersStore usersStore;
    private final FilesService filesService;

    @Inject
    public ToDosService(ToDosStore toDosStore, UsersStore usersStore, FilesService filesService) {
        this.toDosStore = toDosStore;
        this.usersStore = usersStore;
        this.filesService = filesService;
    }

    public List<ToDoResponse> getAll() {
        List<? extends MongoDbToDo> toDos = toDosStore.getAll();
        return toDos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<List<ToDoResponse>> getToDosByUser(String username, ObjectId userId) {
        var user = usersStore.getByUsername(username);
        if (user == null || !user.getId().equals(userId)) {
            return Optional.empty();
        }

        List<MongoDbToDo> toDos = toDosStore.getByUserId(userId);
        return Optional.of(toDos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    public Optional<ToDoResponse> getToDoById(String toDoId, ObjectId userId) {
        MongoDbToDo toDo = toDosStore.getById(new org.bson.types.ObjectId(toDoId));
        if (toDo == null || !toDo.getUserId().equals(userId)) {
            return Optional.empty();
        }
        return Optional.of(mapToResponse(toDo));
    }

    public ToDoResponse createToDo(CreateToDoRequest request, ObjectId userId, List<Http.MultipartFormData.FilePart<File>> files) throws IOException, NoSuchAlgorithmException {
        if (!request.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User ID in token does not match user ID in request");
        }

        List<FileMetadata> uploadedFiles = filesService.create(files);

        ToDo toDo = ToDo.create(request.getUserId(), request.getTitle(), request.getDescription(), request.getTags(), uploadedFiles);
        MongoDbToDo createdToDo = toDosStore.create(toDo);
        return mapToResponse(createdToDo);
    }

    public ToDoResponse updateToDo(ObjectId userId, UpdateToDoRequest updateToDoRequest, List<Http.MultipartFormData.FilePart<File>> fileParts) throws IOException, NoSuchAlgorithmException {
        MongoDbToDo toDoExist = toDosStore.getById(updateToDoRequest.getId());

        if (toDoExist == null) {
            throw new IllegalArgumentException("To-Do not found");
        }

        if (!toDoExist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("To-Do does not belong to user");
        }

        if (filesService.hasFileHashChanged(updateToDoRequest.getFilesMetadata(), toDoExist.getFiles())) {
            throw new IllegalArgumentException("Files can't be added with request");
        }

        List<FileMetadata> filesToDelete = filesService.getFilesToDelete(updateToDoRequest.getFilesMetadata(), toDoExist.getFiles());
        filesService.deleteFiles(filesToDelete);

        List<FileMetadata> uploadedFiles = filesService.uploadFiles(fileParts);
        List<FileMetadata> unchangedFiles = filesService.mergeFiles(updateToDoRequest.getFilesMetadata(), toDoExist.getFiles());
        uploadedFiles.addAll(unchangedFiles);

        ToDo updatedToDo = ToDo.create(
                userId,
                updateToDoRequest.getTitle(),
                updateToDoRequest.getDescription(),
                updateToDoRequest.getTags(),
                uploadedFiles
        );
        updatedToDo.setId(updateToDoRequest.getId());
        toDosStore.update(updatedToDo);

        MongoDbToDo refreshedToDo = toDosStore.getById(updateToDoRequest.getId());
        return mapToResponse(refreshedToDo);
    }

    public void deleteToDoById(String toDoId, ObjectId userId) throws Exception {
        MongoDbToDo toDo = toDosStore.getById(new org.bson.types.ObjectId(toDoId));
        if (toDo == null || !toDo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("To-Do not found or does not belong to user");
        }
        filesService.deleteFiles(toDo.getFiles());
        toDosStore.removeById(new org.bson.types.ObjectId(toDoId));
    }

    public File exportToDoById(String toDoId, ObjectId userId) throws Exception {
        MongoDbToDo toDo = toDosStore.getById(new org.bson.types.ObjectId(toDoId));
        if (toDo == null || !toDo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("To-Do not found or does not belong to user");
        }
        return filesService.exportToDoFiles(toDo);
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
