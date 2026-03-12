package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import me.panjohnny.trenovacmaturity.ExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingSelectExamController extends BaseController {
    @FXML
    public FlowPane selectedExams;

    protected final ArrayList<File> selectedExamFiles = new ArrayList<>();

    @Override
    public void loadAppData() {
        selectedExamFiles.clear();
    }

    public void addFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open .maturita file");
        fileChooser.setInitialDirectory(new File(""));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("MATURITA Files", "*.maturita"));
        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null) {
            selectedExamFiles.addAll(files);
            refreshSelectedExams();
        }
    }

    private void refreshSelectedExams() {
       selectedExams.getChildren().clear();
       for (File file : selectedExamFiles) {
           Button fileButton = new Button(file.getName());
           fileButton.setMaxWidth(400);
           fileButton.setPrefWidth(200);
           fileButton.setPrefHeight(200);
           fileButton.setPadding(new Insets(10));
           fileButton.setMnemonicParsing(false);
           fileButton.getStyleClass().add("linkpane");
           fileButton.setOnAction(_ -> {
               selectedExamFiles.remove(file);
               refreshSelectedExams();
           });
           selectedExams.getChildren().add(fileButton);
       }
       // "Add file" button stays at the end
       Button addButton = new Button("+ Přidat soubor(y)");
       addButton.setMaxWidth(400);
       addButton.setPrefWidth(200);
       addButton.setPrefHeight(200);
       addButton.setMnemonicParsing(false);
       addButton.getStyleClass().add("linkpane");
       addButton.setOnAction(_ -> addFile());
       selectedExams.getChildren().add(addButton);
    }

    @FXML
    public void nextStep() {
        try {
            application.startTrainingCreation(selectedExamFiles);
        } catch (IOException e) {
            ExceptionHandler.handleError(e, "Training creation failed");
        }
    }
}
