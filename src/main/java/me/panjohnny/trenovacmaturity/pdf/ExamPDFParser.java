package me.panjohnny.trenovacmaturity.pdf;

import me.panjohnny.trenovacmaturity.fs.Archiver;
import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;
import me.panjohnny.trenovacmaturity.fx.LoadingController;
import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.Question;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ExamPDFParser {

    public Exam parse(File file) throws IOException {
        TemporaryFileSystemManager.cleanup();

        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);

            double approxProgressPerPage = 1.0 / (double) doc.getNumberOfPages();
            double currentProgress = 0.0;

            LinkedHashMap<String, String> regionText = new LinkedHashMap<>();

            BufferedImage metaImage = renderer.renderImageWithDPI(0, ImageUtil.DPI);
            TemporaryFileSystemManager.writeImageRegion("meta", metaImage);

            String metaText = PDFUtil.extractMetaText(doc, 0.07);

            for (int pageIndex = 1; pageIndex < doc.getNumberOfPages(); pageIndex++) {
                PDPage page = doc.getPage(pageIndex);

                BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, ImageUtil.DPI);

                System.out.println("Page " + pageIndex);

                System.out.println("Detecting horizontal lines...");
                List<Integer> horizontalLines = ImageUtil.detectHorizontalLines(pageImage);

                System.out.println("Creating regions...");
                List<Rectangle> regions = createRegions(horizontalLines,
                        (int) page.getMediaBox().getWidth() * ImageUtil.DPI / 72,
                        (int) page.getMediaBox().getHeight() * ImageUtil.DPI / 72);

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                System.out.println("Processing regions...");
                for (int i = 0; i < regions.size(); i++) {
                    Rectangle region = regions.get(i);

                    String regionName = "region_" + pageIndex + "_" + i;

                    stripper.addRegion(regionName, new Rectangle(region.x * 72 / ImageUtil.DPI, region.y * 72 / ImageUtil.DPI, region.width * 72 / ImageUtil.DPI, region.height * 72 / ImageUtil.DPI));

                    BufferedImage regionImage = pageImage.getSubimage(
                            region.x, region.y, region.width, region.height);

                    System.out.println("Processing region " + regionName);
                    regionImage = ImageUtil.removeWhitespace(regionImage);

                    System.out.println("Writing region image " + regionName);

                    TemporaryFileSystemManager.writeImageRegion(regionName, regionImage);

                    currentProgress += approxProgressPerPage / (double) regions.size();
                    LoadingController.setProgress(currentProgress);
                }

                stripper.extractRegions(page);

                System.out.println("Extracting text from regions...");
                for (String region : stripper.getRegions()) {
                    String text = stripper.getTextForRegion(region);
                    regionText.put(region, text);
                }
            }

            metaText = metaText.replace("\n", "_");
            Exam exam = new Exam(metaText);

            int number = 1;
            for (String key : regionText.keySet()) {
                String text = regionText.getOrDefault(key, "").trim();

                System.out.println("Creating question from region " + key);

                // skip final page
                if (text.contains("ZKONTROLUJTE"))
                    continue;

                exam.add(new Question(number, text, ImageCache.getInstance().getImage(key), key));

                number++;
            }

            System.out.println("Exporting archive...");

            Archiver.createArchive(metaText, exam, null, null);

            LoadingController.setProgress(1.0d);

            return exam;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Rectangle> createRegions(List<Integer> lines, int width, int height) {
        List<Rectangle> regions = new ArrayList<>();
        int prevY = 0;
        for (int y : lines) {
            regions.add(new Rectangle(0, prevY, width, y - prevY));
            prevY = y;
        }
        regions.add(new Rectangle(0, prevY, width, height - prevY));
        return regions;
    }

    public CompletableFuture<Exam> parseAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parse(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
