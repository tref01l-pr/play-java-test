package services;

import Contracts.Requests.FileMetadataRequest;
import com.google.inject.Inject;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import play.mvc.Http;
import store.FilesStore;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilesService {
    private final FilesStore filesStore;

    @Inject
    public FilesService(FilesStore filesStore) {
        this.filesStore = filesStore;
    }

    public List<FileMetadata> create(List<Http.MultipartFormData.FilePart<File>> fileParts) throws IOException, NoSuchAlgorithmException {
        List<FileMetadata> uploadedFiles = new ArrayList<>();

        for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
            FileMetadata metadata = create(filePart);
            uploadedFiles.add(metadata);
        }

        return uploadedFiles;
    }

    public FileMetadata create(Http.MultipartFormData.FilePart<File> filePart) throws IOException, NoSuchAlgorithmException {
        return filesStore.create(filePart);
    }

    public List<FileMetadata> uploadFiles(List<Http.MultipartFormData.FilePart<File>> fileParts) throws IOException, NoSuchAlgorithmException {
        List<FileMetadata> uploadedFiles = new ArrayList<>();
        for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
            FileMetadata metadata = filesStore.create(filePart);
            uploadedFiles.add(metadata);
        }
        return uploadedFiles;
    }

    public List<FileMetadata> mergeFiles(List<FileMetadataRequest> filesFromRequest, List<FileMetadata> existingFiles) {
        return existingFiles.stream()
                .filter(existingFile -> filesFromRequest.stream()
                        .anyMatch(requestFile -> requestFile.getHash().equals(existingFile.getHash())))
                .collect(Collectors.toList());
    }

    public void deleteFiles(List<FileMetadata> files) {
        for (FileMetadata file : files) {
            filesStore.removeByFileMetadata(file);
        }
    }

    public File exportToDoFiles(MongoDbToDo toDo) throws Exception {
        return filesStore.exportFileWithToDo(toDo);
    }

    public boolean hasFileHashChanged(List<FileMetadataRequest> filesFromRequest, List<FileMetadata> filesFromDatabase) {
        return filesFromRequest.stream()
                .anyMatch(f -> filesFromDatabase.stream()
                        .noneMatch(tde -> tde.getHash().equals(f.getHash())));
    }

    public List<FileMetadata> getFilesToDelete(List<FileMetadataRequest> filesFromRequest, List<FileMetadata> filesFromDatabase) {
        return filesFromDatabase.stream()
                .filter(fileInDb -> filesFromRequest.stream()
                        .noneMatch(fileInRequest -> fileInRequest.getHash().equals(fileInDb.getHash())))
                .collect(Collectors.toList());
    }
}
