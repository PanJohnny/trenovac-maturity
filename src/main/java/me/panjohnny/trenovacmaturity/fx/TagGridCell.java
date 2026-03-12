package me.panjohnny.trenovacmaturity.fx;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.GridCell;

public class TagGridCell extends GridCell<String> {
    private final MaturitaController maturitaController;
    // to maintain white color
    private final Button button;
    public TagGridCell(MaturitaController maturitaController) {
        button = new Button();
        setCursor(Cursor.CROSSHAIR);
        setMinSize(100, 10);
        button.setMinSize(100, 10);
        setPrefHeight(10);
        setGraphic(button);
        button.setOnMouseClicked(this::onClick);
        this.maturitaController = maturitaController;
    }

    private void onClick(MouseEvent mouseEvent) {
        maturitaController.addTag(getItem());
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            button.setText("");
        } else {
            button.setText(item);
        }
    }
}
