package store.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bson.types.ObjectId;

import java.awt.*;
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

    public FileMetadata create(Http.MultipartFormData.FilePart filePart) throws IOException, NoSuchAlgorithmException {
        String fileName = filePart.getFilename();
        String contentType = filePart.getContentType();
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileType(contentType);

        play.libs.Files.TemporaryFile tempFile = (play.libs.Files.TemporaryFile) filePart.getRef();
        File file = tempFile.path().toFile();

        String pdfHash;
        try (InputStream fileStream = new BufferedInputStream(new FileInputStream(file))) {
            pdfHash = PdfUtils.calculatePdfHash(fileStream);
        }
        metadata.setPdfHash(pdfHash);

        List<ObjectId> imageIds = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file)) {
            int numberOfPages = document.getNumberOfPages();
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            Logger.info("Number of pages: " + numberOfPages);

            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");

            for (int page = 0; page < numberOfPages; page++) {
                BufferedImage image = null;
                try {
                    RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");
                    System.setProperty("org.apache.pdfbox.rendering.force-transparent-white", "true");

                    image = pdfRenderer.renderImageWithDPI(
                            page,
                            calculateOptimalDPI(document.getPage(page)),
                            ImageType.RGB);

                    File tempImageFile = File.createTempFile("page-" + page, ".png");
                    try {
                        ImageIO.write(image, "png", tempImageFile);

                        image.flush();
                        image = null;

                        try (InputStream imageStream = new BufferedInputStream(new FileInputStream(tempImageFile))) {
                            ObjectId imageId = mongoDb.getGridFSBucket().uploadFromStream(
                                    "page-" + page + ".png",
                                    imageStream
                            );
                            imageIds.add(imageId);
                        }
                    } finally {
                        tempImageFile.delete();
                    }

                } catch (IOException e) {
                    if (image != null) {
                        image.flush();
                    }
                    throw e;
                } catch (Exception e) {
                    Logger.error("Failed to create image from PDF page " + page, e);
                    throw new Exception("Failed to create images from PDF", e);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to create images from PDF", e);
        }

        metadata.setImageIds(imageIds);
        return metadata;
    }

    private int calculateOptimalDPI(PDPage page) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        float width = mediaBox.getWidth();
        float height = mediaBox.getHeight();

        double area = width * height;
        if (area > 1000000) {
            return 150;
        }
        return 300;
    }

    @Override
    public void removeByFileMetadata(FileMetadata metadata) {
        for (ObjectId imageId : metadata.getImageIds()) {
            mongoDb.getGridFSBucket().delete(imageId);
        }
    }

    @Override
    public File exportFileWithToDo(MongoDbToDo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("ToDo cannot be null");
        }

        File zipFile;
        try {
            zipFile = Files.createTempFile("todo-export-", ".zip").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file", e);
        }

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            addToDoDataToZip(zos, todo);

            if (todo.getFiles() != null) {
                for (FileMetadata fileMetadata : todo.getFiles()) {
                    addImagesAsPngFromGridFS(zos, fileMetadata);
                }
            }

        } catch (IOException e) {
            Logger.error(e.getMessage());
            throw new RuntimeException("Failed to export ToDo to ZIP", e);
        }

        return zipFile;
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