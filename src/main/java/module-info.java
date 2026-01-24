module me.panjohnny.trenovacmaturity {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires atlantafx.base;
    requires org.jetbrains.annotations;
    requires com.google.gson;

    opens me.panjohnny.trenovacmaturity to javafx.fxml;
    exports me.panjohnny.trenovacmaturity;
    exports me.panjohnny.trenovacmaturity.image;
    opens me.panjohnny.trenovacmaturity.image to javafx.fxml;
    exports me.panjohnny.trenovacmaturity.fx;
    opens me.panjohnny.trenovacmaturity.fx to javafx.fxml;
}