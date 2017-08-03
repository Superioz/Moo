package de.superioz.moo.manager;

import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.client.Moo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;

public class Manager extends Application {

    private static final String APPLICATION_SCENE_TITLE = "MooManager";
    private static Manager instance;

    /**
     * Returns the instance of the manager
     *
     * @return The manager
     */
    public static Manager getInstance() {
        return instance;
    }

    @Getter
    private final MooLogger logger = new MooLogger(APPLICATION_SCENE_TITLE);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        instance = this;

        // initialise moo logger
        Moo.initialise(logger);

        // get root pane
        Pane root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        // closing threads on close
        stage.setOnCloseRequest(t -> {
            logger.close();
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(root);
        stage.setTitle(APPLICATION_SCENE_TITLE);
        stage.setScene(scene);
        stage.show();
    }

}
