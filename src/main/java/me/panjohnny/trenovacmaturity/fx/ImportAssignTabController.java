package me.panjohnny.trenovacmaturity.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import me.panjohnny.trenovacmaturity.model.Answer;
import me.panjohnny.trenovacmaturity.model.AnswerSet;
import me.panjohnny.trenovacmaturity.model.Question;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportAssignTabController extends BaseController {
    private final Question question;
    private final AnswerSet answers;
    private final ImportAssignController parentController;
    private Tab tab;

    private Label questionNumberLabel;
    private Label questionTextLabel;
    private Label questionMetadataLabel;
    private Button prevButton;
    private Button nextButton;
    private Button doneButton;
    private Label questionImageLabel;
    private ImageView questionImageView;
    private FlowPane answersGrid;
    private ScrollPane answersScrollPane;

    private List<Answer> selected;

    public ImportAssignTabController(Question question, AnswerSet answers, ImportAssignController parentController, List<Answer> selected) {
        this.question = question;
        this.answers = answers;
        this.parentController = parentController;
        this.selected = selected;
    }

    private Set<Integer> selectedAnswerIndices = new HashSet<>();
    private int currentQuestionIndex;

    @Override
    public void loadAppData() {
        // No app data to load here
    }

    public void initializeTab() {
        updateQuestionDisplay();
        updateAnswersGrid();
        updateNavigationButtons();
    }

    private void updateQuestionDisplay() {
        questionNumberLabel.setText("Otázka " + (currentQuestionIndex + 1));
        questionTextLabel.setText(question.text());

        // Display metadata if available
        StringBuilder metadata = new StringBuilder();
        metadata.append(question.tags());
        questionMetadataLabel.setText(metadata.toString());
    }

    private void updateAnswersGrid() {
        answersGrid.getChildren().clear();

        for (int i = 0; i < answers.size(); i++) {
            String answerText = answers.get(i).text();
            var answerImage = answers.get(i).image();

            StackPane answerBox = createAnswerBox(answerImage, answers.get(i).number());
            answersGrid.getChildren().add(answerBox);
        }

        // Scroll to relevant answers (currentQuestionIndex - 2 to currentQuestionIndex + 2)
        scrollToRelevantAnswers();
    }

    private StackPane createAnswerBox(Image image, int index) {
        StackPane box = new StackPane();
        box.setPrefSize(400, 200);
        box.setMinSize(400, 200);
        box.setMaxSize(400, 400);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        Label content = new Label("Odpověď " + index);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(400);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
            content.setGraphic(imageView);
            content.setContentDisplay(ContentDisplay.TOP);
        }

        box.getChildren().add(content);

        // Set initial style
        updateBoxStyle(box, selectedAnswerIndices.contains(index));

        // Handle click
        box.setOnMouseClicked(event -> {
            if (selectedAnswerIndices.contains(index)) {
                selectedAnswerIndices.remove(index);
            } else {
                selectedAnswerIndices.add(index);
            }
            updateBoxStyle(box, selectedAnswerIndices.contains(index));
        });

        if (selected.stream().anyMatch(a -> a.number() == index)) {
            selectedAnswerIndices.add(index);
            updateBoxStyle(box, true);
        }

        return box;
    }

    private void updateBoxStyle(StackPane box, boolean selected) {
        if (selected) {
            box.setStyle("-fx-border-color: #2196F3; -fx-border-width: 3; -fx-cursor: hand;");
        } else {
            box.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-cursor: hand;");
        }
    }

    private void scrollToRelevantAnswers() {
        // Scroll to show answers around current question (e.g., question 5 -> answers 3-7)
        int targetIndex = Math.max(0, currentQuestionIndex - 2);

        javafx.application.Platform.runLater(() -> {
            double totalAnswers = answers.size();
            if (totalAnswers > 0) {
                double scrollPosition = Math.min(1.0, targetIndex / totalAnswers);
                answersScrollPane.setVvalue(scrollPosition / 2); // Adjusted for better centering
            }
        });
    }

    private void handlePrevious() {
        parentController.goToPreviousQuestion();
    }

    private void handleNext() {
        parentController.goToNextQuestion();
    }

    private void updateNavigationButtons() {
        prevButton.setDisable(currentQuestionIndex == 0);
        // nextButton disable logic depends on total questions count from parent
    }

    public void setCurrentQuestionIndex(int index) {
        this.currentQuestionIndex = index;
        if (questionNumberLabel != null) {
            updateQuestionDisplay();
            updateNavigationButtons();
            scrollToRelevantAnswers();
        }
    }

    public Set<Integer> getSelectedAnswerIndices() {
        return new HashSet<>(selectedAnswerIndices);
    }

    public void setSelectedAnswerIndices(Set<Integer> indices) {
        this.selectedAnswerIndices = new HashSet<>(indices);
        if (answersGrid != null) {
            updateAnswersGrid();
        }
    }

    public Tab getTab() {
        if (tab == null) {
            tab = new Tab();
            tab.setText("Otázka " + (currentQuestionIndex + 1));
            tab.setContent(createContent());
        }
        return tab;
    }

    private AnchorPane createContent() {
        AnchorPane root = new AnchorPane();
        root.setMinHeight(0.0);
        root.setMinWidth(0.0);
        root.setPrefHeight(400.0);
        root.setPrefWidth(600.0);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.35);
        AnchorPane.setBottomAnchor(splitPane, 0.0);
        AnchorPane.setLeftAnchor(splitPane, 0.0);
        AnchorPane.setRightAnchor(splitPane, 0.0);
        AnchorPane.setTopAnchor(splitPane, 0.0);

        // Left side - question metadata and controls
        AnchorPane leftPane = new AnchorPane();
        leftPane.setMinHeight(0.0);
        leftPane.setMinWidth(0.0);
        leftPane.setPrefHeight(160.0);
        leftPane.setPrefWidth(100.0);

        VBox leftVBox = new VBox(15.0);
        leftVBox.setPadding(new Insets(20.0));
        AnchorPane.setBottomAnchor(leftVBox, 0.0);
        AnchorPane.setLeftAnchor(leftVBox, 0.0);
        AnchorPane.setRightAnchor(leftVBox, 0.0);
        AnchorPane.setTopAnchor(leftVBox, 0.0);

        questionNumberLabel = new Label("Otázka " + (currentQuestionIndex + 1));
        questionNumberLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Separator separator1 = new Separator();

        questionTextLabel = new Label("Text otázky");
        questionTextLabel.setWrapText(true);

        questionMetadataLabel = new Label("Metadata");
        questionMetadataLabel.setWrapText(true);
        questionMetadataLabel.setStyle("-fx-text-fill: gray;");

        questionImageLabel = new Label("Obrázek");
        questionImageLabel.setWrapText(true);

        questionImageView = new ImageView(question.image());
        questionImageView.setFitWidth(400);
        questionImageView.setPreserveRatio(true);
        questionImageLabel.setGraphic(questionImageView);
        questionImageLabel.setContentDisplay(ContentDisplay.TOP);
        leftVBox.getChildren().add(questionImageLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Separator separator2 = new Separator();

        HBox buttonBox = new HBox(10.0);
        buttonBox.setAlignment(Pos.CENTER);

        prevButton = new Button("Předchozí");
        prevButton.setOnAction(e -> handlePrevious());

        nextButton = new Button("Další");
        nextButton.setOnAction(e -> handleNext());

        doneButton = new Button("Zavřít a uložit");
        doneButton.setOnAction(e -> parentController.saveAndClose());

        buttonBox.getChildren().addAll(prevButton, doneButton, nextButton);

        leftVBox.getChildren().addAll(
            questionNumberLabel,
            separator1,
            questionTextLabel,
            questionMetadataLabel,
            spacer,
            separator2,
            buttonBox
        );

        leftPane.getChildren().add(leftVBox);

        // Right side - answers grid
        AnchorPane rightPane = new AnchorPane();
        rightPane.setMinHeight(0.0);
        rightPane.setMinWidth(0.0);
        rightPane.setPrefHeight(303.0);
        rightPane.setPrefWidth(480.0);

        VBox rightVBox = new VBox(10.0);
        rightVBox.setPadding(new Insets(20.0));
        AnchorPane.setBottomAnchor(rightVBox, 0.0);
        AnchorPane.setLeftAnchor(rightVBox, 0.0);
        AnchorPane.setRightAnchor(rightVBox, 0.0);
        AnchorPane.setTopAnchor(rightVBox, 0.0);

        Label answersLabel = new Label("Vyberte jednu, nebo několik odpovědí");
        answersLabel.setStyle("-fx-font-weight: bold;");

        answersScrollPane = new ScrollPane();
        answersScrollPane.setFitToWidth(true);
        VBox.setVgrow(answersScrollPane, Priority.ALWAYS);

        answersGrid = new FlowPane();
        answersGrid.setHgap(10.0);
        answersGrid.setVgap(10.0);

        answersScrollPane.setContent(answersGrid);

        rightVBox.getChildren().addAll(answersLabel, answersScrollPane);
        rightPane.getChildren().add(rightVBox);

        splitPane.getItems().addAll(leftPane, rightPane);
        root.getChildren().add(splitPane);

        return root;
    }

    public void modifyMap(QuestionAnswerMap map) {
        List<Answer> ans = new ArrayList<>();

        for (Integer selectedAnswerIndex : selectedAnswerIndices) {
            ans.add(answers.get(selectedAnswerIndex - 1));
        }

        map.put(question, ans);
    }
}
