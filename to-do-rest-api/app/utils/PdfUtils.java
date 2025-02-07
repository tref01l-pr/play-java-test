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

    private static PDDocument reconstructFromBinaryData(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = fis.readAllBytes();

            // Convert raw image data into PDF
            ByteArrayOutputStream pdf = new ByteArrayOutputStream();

            // PDF header
            pdf.write("%PDF-1.7\n".getBytes());

            // Object 1 - Catalog
            pdf.write("1 0 obj\n<</Type/Catalog/Pages 2 0 R>>\nendobj\n".getBytes());

            // Object 2 - Pages
            pdf.write("2 0 obj\n<</Type/Pages/Kids[3 0 R]/Count 1>>\nendobj\n".getBytes());

            // Object 3 - Page
            pdf.write(("3 0 obj\n<</Type/Page/Parent 2 0 R/Resources<</XObject<</Im0 4 0 R>>" +
                    "/ProcSet[/PDF/ImageC]>>/MediaBox[0 0 4096 4096]/Contents 5 0 R>>\nendobj\n").getBytes());

            // Object 4 - Image
            pdf.write(("4 0 obj\n<</Type/XObject/Subtype/Image/Width 4096/Height 4096" +
                    "/ColorSpace/DeviceRGB/BitsPerComponent 8/Length " + data.length +
                    "/Filter/DCTDecode>>\nstream\n").getBytes());
            pdf.write(data);
            pdf.write("\nendstream\nendobj\n".getBytes());

            // Object 5 - Contents
            String contents = "q\n4096 0 0 4096 0 0 cm\n/Im0 Do\nQ\n";
            pdf.write(("5 0 obj\n<</Length " + contents.length() + ">>\nstream\n" + contents + "\nendstream\nendobj\n").getBytes());

            // Cross-reference table
            long startxref = pdf.size();
            pdf.write("xref\n0 6\n0000000000 65535 f\n".getBytes());
            pdf.write(String.format("%010d 00000 n\n", 1).getBytes());
            pdf.write(String.format("%010d 00000 n\n", 2).getBytes());
            pdf.write(String.format("%010d 00000 n\n", 3).getBytes());
            pdf.write(String.format("%010d 00000 n\n", 4).getBytes());
            pdf.write(String.format("%010d 00000 n\n", 5).getBytes());

            // Trailer
            pdf.write(("trailer\n<</Size 6/Root 1 0 R>>\nstartxref\n" + startxref + "\n%%EOF").getBytes());

            return Loader.loadPDF(pdf.toByteArray());
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
}
