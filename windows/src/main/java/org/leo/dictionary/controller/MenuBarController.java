package org.leo.dictionary.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.DBWordProvider;
import org.leo.dictionary.word.provider.WordProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class MenuBarController {
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem useDbMenuItem;
    private final Callback<Class<?>, Object> controllerFactory;
    private final MenuBarActions actions;

    @Inject
    public MenuBarController(Callback<Class<?>, Object> controllerFactory, WordProvider wordProvider, DBWordProvider dbWordProvider) {
        this.controllerFactory = controllerFactory;
        this.actions = new MenuBarActions(wordProvider, dbWordProvider, this::ownerWindow);
    }

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
            stage.initOwner(ownerWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnCloseRequest(event -> controller.onClose());
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(Supplier<WordCriteria> criteriaSupplier, Runnable onDataChanged, Runnable onProviderChanged) {
        Objects.requireNonNull(criteriaSupplier);
        Objects.requireNonNull(onDataChanged);
        Objects.requireNonNull(onProviderChanged);
        actions.bindUseDbMenuItem(useDbMenuItem);
        actions.init(criteriaSupplier, onDataChanged, onProviderChanged);
    }

    public void parseWords(ActionEvent actionEvent) {
        actions.parseWords();
    }

    public void useDb(ActionEvent actionEvent) {
        actions.useDb();
    }

    public void importWords(ActionEvent actionEvent) {
        actions.importWords();
    }

    public void cleanDb(ActionEvent actionEvent) {
        actions.cleanDb();
    }

    public void setKnowledge(ActionEvent actionEvent) {
        actions.setKnowledge();
    }

    public void exportWordsToFile(ActionEvent actionEvent) {
        actions.exportWordsToFile();
    }

    public void importWordsFromFile(ActionEvent actionEvent) {
        actions.importWordsFromFile();
    }

    private Window ownerWindow() {
        return menuBar.getScene().getWindow();
    }
}
