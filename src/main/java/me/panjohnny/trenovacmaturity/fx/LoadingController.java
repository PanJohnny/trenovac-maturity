package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import me.panjohnny.trenovacmaturity.MaturitaApplication;

public class LoadingController implements BaseController {
    @FXML
    public ProgressBar progress;

    private static LoadingController instance;

    public static synchronized void setProgress(double value) {
        if (instance != null) {
            instance.progress.setProgress(value);
        }
    }


    @Override
    public void setApplication(MaturitaApplication application) {

    }

    @Override
    public void loadAppData() {
        instance = this;
    }
}
