package store.mongodb;

import CustomExceptions.DatabaseException;
import CustomExceptions.FileProcessingException;
import CustomExceptions.ResourceNotFoundException;
import CustomExceptions.ServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bson.types.ObjectId;

import java.nio.file.Files;

import play.Logger;
import play.mvc.Http;
import services.MongoDb;
import store.FilesStore;
import utils.PdfUtils;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MongoDbFilesStore implements FilesStore {
    @Inject
    private MongoDb mongoDb;

    @Override
    public FileMetadata create(Http.MultipartFormData.FilePart filePart) {
        try {
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(filePart.getFilename());
            metadata.setFileType(filePart.getContentType());

            File inputFile = ((play.libs.Files.TemporaryFile) filePart.getRef()).path().toFile();
            PDDocument document = null;

            try {
                document = PdfUtils.loadAndRepairPDF(inputFile);
                metadata.setPdfHash(calculatePdfHash(inputFile));

                List<ObjectId> imageIds = processDocument(document);
                if (imageIds.isEmpty()) {
                    throw new FileProcessingException("No images found in PDF");
                }

                metadata.setImageIds(imageIds);
                return metadata;
            } finally {
                if (document != null) {
                    document.close();
                }
            }
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while creating file metadata", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while creating file metadata", e);
            throw new DatabaseException("Error accessing database", e);
        } catch (IOException | NoSuchAlgorithmException e) {
            Logger.error("Error processing PDF file", e);
            throw new FileProcessingException("Error processing PDF file: " + e.getMessage());
        }
    }

    @Override
    public void removeByFileMetadata(FileMetadata metadata) {
        try {
            for (ObjectId imageId : metadata.getImageIds()) {
                mongoDb.getGridFSBucket().delete(imageId);
            }
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while removing file metadata", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while removing file metadata", e);
            throw new DatabaseException("Error accessing database", e);
        }
    }

    @Override
    public File exportFileWithToDo(MongoDbToDo todo) {
        if (todo == null) {
            throw new ResourceNotFoundException("ToDo cannot be null");
        }

        try {
            File zipFile = Files.createTempFile("todo-export-", ".zip").toFile();

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                addToDoDataToZip(zos, todo);

                if (todo.getFiles() != null) {
                    for (FileMetadata fileMetadata : todo.getFiles()) {
                        addImagesAsPngFromGridFS(zos, fileMetadata);
                    }
                }

                return zipFile;
            }
        } catch (MongoTimeoutException e) {
            Logger.error("Database timeout while exporting todo", e);
            throw new ServiceUnavailableException("Database is temporarily unavailable", e);
        } catch (MongoException e) {
            Logger.error("Database error while exporting todo", e);
            throw new DatabaseException("Error accessing database", e);
        } catch (IOException e) {
            Logger.error("Error creating export file", e);
            throw new FileProcessingException("Error creating export file: " + e.getMessage(), e);
        }
    }

    private List<ObjectId> processDocument(PDDocument document) throws IOException {
        List<ObjectId> imageIds = new ArrayList<>();
        List<BufferedImage> images = PdfUtils.convertPDFToImages(document);

        for (int i = 0; i < images.size(); i++) {
            imageIds.add(processImage(images.get(i), i));
            images.get(i).flush();
        }

        return imageIds;
    }

    private ObjectId processImage(BufferedImage image, int pageNumber) throws IOException {
        File tempImageFile = null;
        try {
            tempImageFile = File.createTempFile("page-" + pageNumber, ".png");
            ImageIO.write(image, "png", tempImageFile);

            try (InputStream imageStream = new BufferedInputStream(new FileInputStream(tempImageFile))) {
                return mongoDb.getGridFSBucket().uploadFromStream("page-" + pageNumber + ".png", imageStream);
            }
        } finally {
            if (tempImageFile != null) {
                tempImageFile.delete();
            }
        }
    }

    private String calculatePdfHash(File file) throws IOException, NoSuchAlgorithmException {
        try (InputStream fileStream = new BufferedInputStream(new FileInputStream(file))) {
            return PdfUtils.calculatePdfHash(fileStream);
        }
    }

    private void addToDoDataToZip(ZipOutputStream zos, MongoDbToDo todo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(todo);

        ZipEntry entry = new ZipEntry("todo.json");
        zos.putNextEntry(entry);
        zos.write(jsonData.getBytes());
        zos.closeEntry();
    }

    private void addImagesAsPngFromGridFS(ZipOutputStream zos, FileMetadata fileMetadata) throws IOException {
        GridFSBucket gridFSBucket = mongoDb.getGridFSBucket();

        if (fileMetadata.getImageIds() == null || fileMetadata.getImageIds().isEmpty()) {
            throw new FileNotFoundException("No image IDs found in FileMetadata");
        }

        for (ObjectId imageId : fileMetadata.getImageIds()) {
            try (GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(imageId)) {
                if (downloadStream == null) {
                    throw new FileNotFoundException("File with ID " + imageId + " not found in GridFS");
                }

                BufferedImage image = ImageIO.read(downloadStream);
                if (image == null) {
                    throw new IOException("Failed to decode image with ID " + imageId + " as an image.");
                }

                String fileName = "images/" + imageId.toHexString() + ".png";
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "png", baos);
                    zos.write(baos.toByteArray());
                } finally {
                    zos.closeEntry();
                }
            }
        }
    }
}