package me.panjohnny.trenovacmaturity.pdf;

import me.panjohnny.trenovacmaturity.ExceptionHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;

public class PDFUtil {
    public static String extractMetaText(PDDocument doc, double areaHeight) throws IOException {
        PDFTextStripperByArea metaStripper = new PDFTextStripperByArea();
        metaStripper.setSortByPosition(true);
        metaStripper.addRegion("meta", new Rectangle(0, 0,
                (int) doc.getPage(0).getMediaBox().getWidth() * ImageUtil.DPI / 72,
                (int) (doc.getPage(0).getMediaBox().getHeight() * ImageUtil.DPI / 72 * areaHeight)));
        metaStripper.extractRegions(doc.getPage(0));
        return metaStripper.getTextForRegion("meta").trim();
    }

    public static String calculateDocumentFootprint(File file) {
        MessageDigest digest;
        String fallbackString = Integer.toHexString(file.getAbsolutePath().hashCode());
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            ExceptionHandler.handleError(e, "Failed to initialize MD5 digest for PDF footprint calculation, using fallback method");
            return fallbackString;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException e) {
            ExceptionHandler.handleError(e, "Failed to read PDF file for footprint calculation, using fallback method");
            return fallbackString;
        }
    }
}
