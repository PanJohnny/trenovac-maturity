package me.panjohnny.trenovacmaturity.pdf;

import me.panjohnny.trenovacmaturity.fx.Actions;
import me.panjohnny.trenovacmaturity.model.Question;
import me.panjohnny.trenovacmaturity.model.training.Training;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Calendar;

public class TrainingExporter {
    public static void createPDF(Training training) {
        try (PDDocument document = new PDDocument()) {
            var info = document.getDocumentInformation();
            info.setTitle(training.getMeta());
            info.setCreator("Trénovač Maturity");
            info.setAuthor(System.getProperty("user.name") + "/CERMAT");
            info.setCreationDate(Calendar.getInstance());
            info.setProducer(System.getProperty("user.name"));
            info.setSubject("Trénink na maturitu");
            info.setKeywords("maturita, trénink, " + training.getMeta());

            PDPage currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);

            int y = 0;

            for (Question question : training) {
                Path imagePath = Path.of(URI.create(question.image().getUrl()));

                // add the image to the PDF at the correct position

                PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath.toString(), document);
                try (var contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, false)) {
                    int calculatedHeight = (int) (pdImage.getHeight() * (currentPage.getMediaBox().getWidth() / pdImage.getWidth()));

                    // If the image is too tall to fit on the current page, start a new page
                    if (y + calculatedHeight > currentPage.getMediaBox().getHeight()) {
                        currentPage = new PDPage(PDRectangle.A4);
                        document.addPage(currentPage);
                        y = 0;
                    }

                    contentStream.drawImage(pdImage, 0, y, currentPage.getMediaBox().getWidth(), calculatedHeight);
                    y+= calculatedHeight;
                }
                if (y > currentPage.getMediaBox().getHeight()) {
                    currentPage = new PDPage(PDRectangle.A4);
                    document.addPage(currentPage);
                    y = 0;
                }
            }

            document.save(training.getMeta() + ".pdf");
            Actions.openFileOrUrl(Path.of(training.getMeta() + ".pdf").toFile(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
