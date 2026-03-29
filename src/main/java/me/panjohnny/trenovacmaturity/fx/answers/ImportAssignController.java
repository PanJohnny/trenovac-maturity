package me.panjohnny.trenovacmaturity.fx.answers;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import me.panjohnny.trenovacmaturity.View;
import me.panjohnny.trenovacmaturity.fx.BaseController;
import me.panjohnny.trenovacmaturity.model.answer.Answer;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;

import java.util.ArrayList;
import java.util.List;

public class ImportAssignController extends BaseController {
    @FXML
    public TabPane tabPane;

    private List<ImportAssignTabController> tabControllers;

    @Override
    public void loadAppData() {
        // Get exam and answers
        application.exam().loadQuestionAnswerMap();
        this.tabControllers = new ArrayList<>();

        // For each question in the exam, create a new tab
        int questionIndex = 0;
        int firstEmptyIndex = -1;
        for (var question : application.getExam()) {
            List<Answer> answers = application.getQuestionAnswerMap().getOrDefault(question, List.of());
            if (firstEmptyIndex == -1 && answers.isEmpty()) {
                firstEmptyIndex = questionIndex;
            }
            ImportAssignTabController tabController = new ImportAssignTabController(question, application.getAnswers(), this, answers);
            tabController.setApplication(application);
            tabController.setCurrentQuestionIndex(questionIndex);
            var tab = tabController.getTab();
            tabController.initializeTab();

            tabPane.getTabs().add(tab);
            tabControllers.add(tabController);
            questionIndex++;
        }

        if (firstEmptyIndex != -1) {
            tabPane.getSelectionModel().select(firstEmptyIndex);
            Dialog<Void> foundDialog = new Dialog<>();
            foundDialog.setTitle("Ups... nepovedlo se přiřadit odpověď");
            foundDialog.setContentText("Jedna nebo více otázek nebylo možné automaticky přiřadit k odpovědím. Byla vybrána první taková otázka, kterou je potřeba přiřadit ručně.");
            foundDialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK);
            foundDialog.initOwner(application.getPrimaryStage().getScene().getWindow());
            foundDialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            foundDialog.showAndWait();
        } else {
            tabPane.getSelectionModel().select(0);
        }
    }

    public void saveAndClose() {
        QuestionAnswerMap map = application.getQuestionAnswerMap();
        for (ImportAssignTabController tabController : tabControllers) {
            tabController.modifyMap(map);
        }

        application.saveOpened();
        application.changeView(View.WELCOME);
    }

    public void goToPreviousQuestion() {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            tabPane.getSelectionModel().select(currentIndex - 1);
        }
    }

    public void goToNextQuestion() {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex < tabPane.getTabs().size() - 1) {
            tabPane.getSelectionModel().select(currentIndex + 1);
        }
    }
}
