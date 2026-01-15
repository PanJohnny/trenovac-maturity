package me.panjohnny.trenovacmaturity.pdf;

import javafx.scene.image.Image;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.Question;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.ICOSVisitor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static me.panjohnny.trenovacmaturity.MaturitaApplication.relativeLocation;

public class PDFExtractor {
    public static final int DPI = 150;

    public Exam parse(File file) throws IOException {
        File relative = relativeLocation.toFile();

        if (!relative.exists()) {
            Files.createDirectories(relativeLocation);
        } else {
            var files = relative.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.delete()) {
                        System.getLogger("PDFExtractor").log(System.Logger.Level.WARNING, "Could not delete file: " + f.getAbsolutePath());
                    }
                }
            }
        }

        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);

            LinkedHashMap<String, String> regionText = new LinkedHashMap<>();
            LinkedHashMap<String, Image> regionImages = new LinkedHashMap<>();

            PDFTextStripperByArea metaStripper = new PDFTextStripperByArea();
            metaStripper.setSortByPosition(true);
            metaStripper.addRegion("meta", new Rectangle(0, 0,
                    (int) doc.getPage(0).getMediaBox().getWidth() * DPI / 72,
                    (int) (doc.getPage(0).getMediaBox().getHeight() * DPI / 72 * 0.07)));
            BufferedImage metaImage = renderer.renderImageWithDPI(0, DPI);
            File metaFile = new File(relative, "meta.png");
            ImageIO.write(metaImage, "png", metaFile);
            metaStripper.extractRegions(doc.getPage(0));
            String metaText = metaStripper.getTextForRegion("meta").trim();

            for (int pageIndex = 1; pageIndex < doc.getNumberOfPages(); pageIndex++) {
                PDPage page = doc.getPage(pageIndex);

                BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, DPI);

                List<Integer> horizontalLines = detectHorizontalLines(pageImage);

                List<Rectangle> regions = createRegions(horizontalLines,
                        (int) page.getMediaBox().getWidth() * DPI / 72,
                        (int) page.getMediaBox().getHeight() * DPI / 72);

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                for (int i = 0; i < regions.size(); i++) {
                    Rectangle region = regions.get(i);

                    if (region.height < 150) // Minimální výška oblasti pro extrakci
                        continue;

                    String regionName = "region_" + pageIndex + "_" + i;

                    stripper.addRegion(regionName, new Rectangle(region.x * 72 / DPI, region.y * 72 / DPI, region.width * 72 / DPI, region.height * 72 / DPI));

                    BufferedImage regionImage = pageImage.getSubimage(
                            region.x, region.y, region.width, region.height);

                    File ff = new File(relative, regionName + ".png");
                    ImageIO.write(regionImage, "png", ff);

                    regionImages.put(regionName, new Image(ff.toURI().toString()));
                }

                stripper.extractRegions(page);

                for (String region : stripper.getRegions()) {
                    String text = stripper.getTextForRegion(region);
                    regionText.put(region, text);
                }
            }

            ArrayList<Question> questions = new ArrayList<>();

            int number = 1;
            for (String key : regionImages.keySet()) {
                Image image = regionImages.get(key);
                String text = regionText.getOrDefault(key, "").trim();

                questions.add(new Question(number, text, image, key));

                number++;
            }

            Exam exam = new Exam(questions);
            File fileOutput = new File(relative,"exam.txt");
            File metaFileOut = new File(relative,"meta.txt");
            java.nio.file.Files.writeString(fileOutput.toPath(), exam.serialize());
            Files.writeString(metaFileOut.toPath(), metaText);

            String name = metaText.replace("\n", "_");

            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(name + ".zip"));

            for (File f : Objects.requireNonNull(relative.listFiles())) {
                ZipEntry entry = new ZipEntry(f.getName());
                zip.putNextEntry(entry);
                byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                zip.write(bytes, 0, bytes.length);
                zip.closeEntry();
            }

            zip.close();

            return exam;
        }
    }

    private List<Integer> detectHorizontalLines(BufferedImage image) {
        List<Integer> lines = new ArrayList<>();

        loopHeight: for (int y = 0; y < image.getHeight(); y++) {
            int consecutiveBlackPixels = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int brightness = (r + g + b) / 3;

                if (brightness < 10) { // Prahová hodnota pro černou barvu
                    consecutiveBlackPixels++;
                } else {
                    if (consecutiveBlackPixels > image.getWidth() * 0.7 && consecutiveBlackPixels < image.getWidth() * 0.77) {
                        lines.add(y);
                        continue loopHeight;
                    }
                    consecutiveBlackPixels = 0;
                }
            }
        }
        return lines;
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
}
