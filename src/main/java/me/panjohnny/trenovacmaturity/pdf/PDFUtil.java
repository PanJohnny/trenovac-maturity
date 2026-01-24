package me.panjohnny.trenovacmaturity.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.*;
import java.io.IOException;

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
}
