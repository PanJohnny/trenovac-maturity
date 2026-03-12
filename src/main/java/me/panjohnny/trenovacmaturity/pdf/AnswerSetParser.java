package me.panjohnny.trenovacmaturity.pdf;

import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;
import me.panjohnny.trenovacmaturity.fx.LoadingController;
import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.answer.Answer;
import me.panjohnny.trenovacmaturity.model.answer.AnswerSet;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AnswerSetParser {
    public AnswerSet parse(File file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);

            // Kód textu: ...
            String meta = PDFUtil.extractMetaText(doc, 0.07);

            String documentFootprint = PDFUtil.calculateDocumentFootprint(file);

            LinkedHashMap<String, String> regionText = new LinkedHashMap<>();

            double currentProgress = 0d;
            double approxProgressPerPage = 1d / (double) doc.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < doc.getNumberOfPages(); pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, ImageUtil.DPI);
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();

                stripper.setSortByPosition(true);

                var lines = ImageUtil.detectHorizontalLinesOfHeight(image, 2 * ImageUtil.DPI / 150);

                for (int i = 0; i < lines.size() - 1; i++) {
                    int y1 = lines.get(i);
                    int y2 = lines.get(i + 1);

                    Rectangle region = new Rectangle(0, y1, image.getWidth(), y2 - y1);
                    BufferedImage regionImage = image.getSubimage(region.x, region.y, region.width, region.height);
                    String regionName = documentFootprint + "_answer_region_" + pageIndex + "_" + i;

                    stripper.addRegion(regionName, new Rectangle(region.x * 72 / ImageUtil.DPI, region.y * 72 / ImageUtil.DPI, region.width * 72 / ImageUtil.DPI, region.height * 72 / ImageUtil.DPI));

                    TemporaryFileSystemManager.writeImageRegion(regionName, regionImage);

                    currentProgress += approxProgressPerPage / (double) lines.size();
                    LoadingController.setProgress(currentProgress);
                }

                stripper.extractRegions(doc.getPage(pageIndex));

                for (String region : stripper.getRegions()) {
                    String text = stripper.getTextForRegion(region);
                    regionText.put(region, text);
                }
            }
            AnswerSet answers = new AnswerSet(meta);

            int answerIndex = 1;
            for (String key : regionText.keySet()) {
                String text = regionText.getOrDefault(key, "").trim();

                // check if text starts with number
                if (text.length() < 3 || !text.contains("b.")) {
                    continue;
                }

                Answer answer = new Answer(answerIndex, text, ImageCache.getInstance().getImage(key), key);
                answers.add(answer);

                answerIndex++;
            }

            answers.sort(Comparator.comparingInt(Answer::number));

            LoadingController.setProgress(1d);

            return answers;
        }
    }

    public CompletableFuture<AnswerSet> parseAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parse(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
