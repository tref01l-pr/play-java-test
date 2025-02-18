package services;

import Contracts.Requests.FileMetadataRequest;
import CustomExceptions.DatabaseException;
import CustomExceptions.ServiceUnavailableException;
import CustomExceptions.ValidationException;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import play.Logger;
import play.mvc.Http;
import store.FilesStore;
import utils.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilesService {
    private final FilesStore filesStore;

    @Inject
    public FilesService(FilesStore filesStore) {
        this.filesStore = filesStore;
    }

    public List<FileMetadata> create(List<Http.MultipartFormData.FilePart<File>> fileParts) {
        try {
            List<FileMetadata> uploadedFiles = new ArrayList<>();

            if (!PdfUtils.IsPdfSplittable(fileParts)) {
                throw new ValidationException("Only PDF files are allowed");
            }

            for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
                FileMetadata metadata = create(filePart);
                uploadedFiles.add(metadata);
            }

            return uploadedFiles;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating files", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating files", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    private FileMetadata create(Http.MultipartFormData.FilePart<File> filePart) {
        return filesStore.create(filePart);
    }

    public List<FileMetadata> uploadFiles(List<Http.MultipartFormData.FilePart<File>> fileParts) {
        try {
            List<FileMetadata> uploadedFiles = new ArrayList<>();

            if (!PdfUtils.IsPdfSplittable(fileParts)) {
                throw new ValidationException("Only PDF files are allowed");
            }

            for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
                FileMetadata metadata = filesStore.create(filePart);
                uploadedFiles.add(metadata);
            }
            return uploadedFiles;
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while uploading files", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while uploading files", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    public List<FileMetadata> mergeFiles(List<FileMetadataRequest> filesFromRequest, List<FileMetadata> existingFiles) {
        return existingFiles.stream()
                .filter(existingFile -> filesFromRequest.stream()
                        .anyMatch(requestFile -> requestFile.getHash().equals(existingFile.getHash())))
                .collect(Collectors.toList());
    }

    public void deleteFiles(List<FileMetadata> files) {
        try {
            for (FileMetadata file : files) {
                filesStore.removeByFileMetadata(file);
            }
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while deleting files", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while deleting files", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    public File exportToDoFiles(MongoDbToDo toDo) {
        try {
            return filesStore.exportFileWithToDo(toDo);
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while exporting files", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while exporting files", e);
            throw new DatabaseException("Error accessing database", e);
        }
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
