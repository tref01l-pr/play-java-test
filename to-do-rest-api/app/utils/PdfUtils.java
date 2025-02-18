package utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.filter.MissingImageReaderException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import play.Logger;
import play.mvc.Http;

import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class PdfUtils {
    private static final Tika tika = new Tika();

    public static boolean isActuallyImage(File file) {
        try {
            String mimeType = tika.detect(file);
            return mimeType.startsWith("image/");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<BufferedImage> convertPDFToImages(PDDocument document) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            try {
                PDPage pdPage = document.getPage(page);
                float dpi = calculateOptimalDPI(pdPage);
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                if (image != null) {
                    images.add(image);
                }
            } catch (MissingImageReaderException e) {
                Logger.warn("Could not process image on page {}: {}", page, e.getMessage());
                continue;
            }
        }

        if (images.isEmpty()) {
            throw new IOException("No pages could be converted to images");
        }

        return images;
    }

    private static float calculateOptimalDPI(PDPage page) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        float width = mediaBox.getWidth();
        float height = mediaBox.getHeight();

        double area = width * height;
        return area > 1000000 ? 150 : 300;
    }

    public static String calculatePdfHash(InputStream pdfInputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = pdfInputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static File getInputFile(Http.MultipartFormData.FilePart filePart) {
        return ((play.libs.Files.TemporaryFile) filePart.getRef()).path().toFile();
    }

    public static boolean IsPdfSplittable(List<Http.MultipartFormData.FilePart<File>> fileParts) {
        for (Http.MultipartFormData.FilePart<File> filePart : fileParts) {
            if (!IsPdfSplittable(getInputFile(filePart))) {
                return false;
            }
        }
        return true;
    }

    public static boolean IsPdfSplittable(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            return document.getNumberOfPages() >= 1;
        } catch (Exception e) {
            Logger.error( e.getMessage());
            try {
                if (isActuallyImage(file)) {
                    return true;
                }
            } catch (Exception ex) {
                Logger.error("Error loading PDF file", e);
            }

            return false;
        }
    }
}
