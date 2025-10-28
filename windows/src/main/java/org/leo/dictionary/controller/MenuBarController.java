package org.leo.dictionary.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.inject.Inject;
import java.io.IOException;

public class MenuBarController {
    @FXML
    private MenuBar menuBar;
    private Callback<Class<?>, Object> controllerFactory;

    @FXML
    private void openConfigWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/leo/dictionary/view/config/config-window.fxml"));
            loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            ConfigWindowController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Configuration");
            stage.setScene(new Scene(root));
            stage.initOwner(menuBar.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            // handle X button
            stage.setOnCloseRequest(event -> controller.onClose());
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    public MenuBarController(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }
}
