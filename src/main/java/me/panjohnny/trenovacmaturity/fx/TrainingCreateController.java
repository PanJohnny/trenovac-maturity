package me.panjohnny.trenovacmaturity.fx;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import me.panjohnny.trenovacmaturity.ExceptionHandler;
import me.panjohnny.trenovacmaturity.fs.Archiver;
import me.panjohnny.trenovacmaturity.model.Training;
import me.panjohnny.trenovacmaturity.model.TrainingBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainingCreateController extends BaseController{
    public CheckBox filterEnable;
    public VBox filtersContainer;
    public TextField trainingName;

    private TrainingBuilder builder;

    @Override
    public void loadAppData() {
        builder = application.getTrainingBuilder();

        for (String tag : builder.getTagCache()) {
            CheckBox checkbox = new CheckBox(tag);
            filtersContainer.getChildren().add(checkbox);
        }
        filtersContainer.setVisible(false);

        filterEnable.setOnAction(_ -> filtersContainer.setVisible(filterEnable.isSelected()));

        trainingName.setText("Trénink " + LocalDate.now());
    }

    public void createAndExportTraining() {
        if (filterEnable.isSelected()) {
            List<String> selectedTags = new ArrayList<>();

            for (Node child : filtersContainer.getChildren()) {
                if (child instanceof CheckBox checkBox) {
                    if (checkBox.isSelected()) {
                        selectedTags.add(checkBox.getText());
                    }
                }
            }

            builder.applyTagFilter(selectedTags);
        }

        String name = trainingName.getText();
        if (name == null || name.isEmpty()) {
            name = "Trénink " + LocalDate.now();
        }
        Training t = builder.create(name);

        try {
            Archiver.createTrainingArchive(t);
        } catch (IOException e) {
            ExceptionHandler.handleSevere(e, "Could not create training archive.");
        }

        application.openTraining(t);
    }
}
