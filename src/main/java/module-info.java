module me.panjohnny.trenovacmaturity {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires atlantafx.base;
    requires org.jetbrains.annotations;
    requires com.google.gson;
    requires org.apache.commons.text;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.carbonicons;

    opens me.panjohnny.trenovacmaturity to javafx.fxml;
    exports me.panjohnny.trenovacmaturity;
    exports me.panjohnny.trenovacmaturity.image;
    opens me.panjohnny.trenovacmaturity.image to javafx.fxml;
    exports me.panjohnny.trenovacmaturity.fx;
    opens me.panjohnny.trenovacmaturity.fx to javafx.fxml;
    exports me.panjohnny.trenovacmaturity.model;
    exports me.panjohnny.trenovacmaturity.model.answer;
    exports me.panjohnny.trenovacmaturity.model.training;
    exports me.panjohnny.trenovacmaturity.fx.training;
    opens me.panjohnny.trenovacmaturity.fx.training to javafx.fxml;
    exports me.panjohnny.trenovacmaturity.fx.node;
    opens me.panjohnny.trenovacmaturity.fx.node to javafx.fxml;
    exports me.panjohnny.trenovacmaturity.fx.answers;
    opens me.panjohnny.trenovacmaturity.fx.answers to javafx.fxml;
    exports me.panjohnny.trenovacmaturity.handler;
    exports me.panjohnny.trenovacmaturity.fs;
}