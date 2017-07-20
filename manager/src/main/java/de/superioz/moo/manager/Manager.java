package de.superioz.moo.manager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Manager extends Application {

    private static final String APPLICATION_SCENE_TITLE = "MooManager";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        stage.setTitle(APPLICATION_SCENE_TITLE);
        stage.setScene(scene);
        stage.show();
    }

}
