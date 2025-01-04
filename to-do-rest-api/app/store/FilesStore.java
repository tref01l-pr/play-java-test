package store;

import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface FilesStore {
    FileMetadata create(Http.MultipartFormData.FilePart<File> filePart) throws IOException, NoSuchAlgorithmException;
    void removeByFileMetadata(FileMetadata metadata);
    File exportFileWithToDo(MongoDbToDo todo);
}
