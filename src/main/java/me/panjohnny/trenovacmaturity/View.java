package me.panjohnny.trenovacmaturity;

public enum View {
    WELCOME("welcome-view"),
    LOADING("loading-view"),
    IN_EXAM("in-exam-view"),
    ANSWERS_IMPORT_ASSIGN("answers-import-assign-view"),
    ANSWERS_IMPORT("answers-import-view"),
    TRAINING_CREATE("training-create-view"),
    TRAINING_SELECT_EXAM("training-select-exam-view"),
    TRAINING_PANIC_START("training-panic-start-view"),
    TRAINING_PANIC("training-panic-view");

    final String file;
    View(String name) {
        this.file = name + ".fxml";
    }

    @Override
    public String toString() {
        return file;
    }
}
