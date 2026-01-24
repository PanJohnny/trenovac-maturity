package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import me.panjohnny.trenovacmaturity.MaturitaApplication;

public abstract class BaseController {
    protected MaturitaApplication application;

    public void setApplication(MaturitaApplication application) {
        this.application = application;
    }

    public abstract void loadAppData();

    @FXML
    public void openZIP() {
        Actions.openZIP(application);
    }

    @FXML
    public void openPDF() {
        Actions.openPDF(application);
    }

    @FXML
    public void openCERMAT() {
        Actions.openCERMAT();
    }

    @FXML
    public void openAnswerSetPDF() {
        Actions.openAnswerSetPDF(application);
    }
}
