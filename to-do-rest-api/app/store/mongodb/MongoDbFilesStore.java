package store.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import entities.mongodb.MongoDbToDo;
import models.FileMetadata;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bson.types.ObjectId;
import java.nio.file.Files;
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

    public FileMetadata create(Http.MultipartFormData.FilePart<File> filePart) throws IOException, NoSuchAlgorithmException {
        String fileName = filePart.getFilename();
        String contentType = filePart.getContentType();
        FileMetadata metadata = new FileMetadata();

        metadata.setFileName(fileName);
        metadata.setFileType(contentType);

        play.libs.Files.TemporaryFile tempFile = (play.libs.Files.TemporaryFile) filePart.getRef();
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
        return metadata;
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
                    addImagesAsJpgFromGridFS(zos, fileMetadata);
                }
            }

        } catch (IOException e) {
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

    private void addFileFromGridFS(ZipOutputStream zos, FileMetadata fileMetadata) throws IOException {
        GridFSBucket gridFSBucket = mongoDb.getGridFSBucket();

        if (fileMetadata.getImageIds() == null || fileMetadata.getImageIds().isEmpty()) {
            throw new FileNotFoundException("No image IDs found in FileMetadata");
        }

        for (ObjectId imageId : fileMetadata.getImageIds()) {
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(imageId);
            if (downloadStream == null) {
                throw new FileNotFoundException("File with ID " + imageId + " not found in GridFS");
            }

            String fileName = "files/" + imageId.toHexString() + ".dat";
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);

            try (InputStream inputStream = downloadStream) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            } finally {
                zos.closeEntry();
                downloadStream.close();
            }
        }
    }

    private void addImagesAsJpgFromGridFS(ZipOutputStream zos, FileMetadata fileMetadata) throws IOException {
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

                String fileName = "images/" + imageId.toHexString() + ".jpg";
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "jpg", baos);
                    zos.write(baos.toByteArray());
                } finally {
                    zos.closeEntry();
                }
            }
        }
    }
}
