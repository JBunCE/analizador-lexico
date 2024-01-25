package org.jbunce.analizadorsintactico;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LexicalAnalyzer extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LexicalAnalyzer.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 910, 610);
        stage.setTitle("Lexical analizer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}