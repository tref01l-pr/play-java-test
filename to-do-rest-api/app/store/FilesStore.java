package store;

import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import play.mvc.Http;

import java.io.File;

public interface FilesStore {
    FileMetadata create(Http.MultipartFormData.FilePart<File> filePart);
    void removeByFileMetadata(FileMetadata metadata);
    File exportFileWithToDo(MongoDbToDo todo);
}
