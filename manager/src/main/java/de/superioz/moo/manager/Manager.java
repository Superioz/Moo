package de.superioz.moo.manager;

import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.client.Moo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Manager extends Application {

    private static final String APPLICATION_SCENE_TITLE = "MooManager";
    private final MooLogger logger = new MooLogger(APPLICATION_SCENE_TITLE);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // initialise moo logger
        Moo.initialise(logger);

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
