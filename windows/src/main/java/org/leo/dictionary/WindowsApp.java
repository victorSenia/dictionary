package org.leo.dictionary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WindowsApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        WindowsAppComponent component = DaggerWindowsAppComponent.create();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/leo/dictionary/view/dictionary_view.fxml"));

        loader.setControllerFactory(component.controllerFactory());

        Scene scene = new Scene(loader.load(), 800, 500);

        stage.setTitle("Dictionary");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}