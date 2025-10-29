package org.leo.dictionary.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.leo.dictionary.WindowsModule;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.*;

import javax.inject.Inject;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MenuBarController {
    @FXML
    private MenuBar menuBar;
    private final Callback<Class<?>, Object> controllerFactory;
    private final WordProvider wordProvider;
    private final DBWordProvider dbWordProvider;

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
    public MenuBarController(Callback<Class<?>, Object> controllerFactory, WordProvider wordProvider, DBWordProvider dbWordProvider) {
        this.controllerFactory = controllerFactory;
        this.wordProvider = wordProvider;
        this.dbWordProvider = dbWordProvider;
    }

    public void findWords(ActionEvent actionEvent) {
        //TODO filter dialog
    }

    public void parseWords(ActionEvent actionEvent) {
        //TODO choose file and config
        ((WordProviderDelegate) wordProvider).setWordProvider(WindowsModule.createFileWordProvider(WindowsModule.getDefaultParseWordsConfig()));
    }

    public void useDb(ActionEvent actionEvent) {
        //TODO check current, filter and update UI
        ((WordProviderDelegate) wordProvider).setWordProvider(dbWordProvider);
    }

    public void importWords(ActionEvent actionEvent) {
        //TODO check current, run in different thread
        dbWordProvider.importWords(wordProvider.findWords(new WordCriteria()));//TODO current WordCriteria
    }

    public void cleanDb(ActionEvent actionEvent) {
        //TODO language picker
        dbWordProvider.deleteWords("de_DE");
    }

    public void setKnowledge(ActionEvent actionEvent) {
        List<Word> words = wordProvider.findWords(new WordCriteria());//TODO current WordCriteria
        double knowledge = 0.;//TODO dialog
        for (Word word : words) {
            word.setKnowledge(knowledge);
        }
        dbWordProvider.updateWord(words);
    }

    public void exportWordsToFile(ActionEvent actionEvent) throws IOException {
        OutputStream outputStream = null;
        WordExporter wordExporter = new WordExporter() {
            @Override
            protected BufferedWriter getBufferedWriter() {
                return new BufferedWriter(new OutputStreamWriter(outputStream));
            }
        };
        //TODO dialog for language and rootTopic and file name
        Topic rootTopic = null;
        String language = "de_DE";
        if (rootTopic != null) {
            wordExporter.writeWords(dbWordProvider.getWordsForLanguage(language, rootTopic), false,
                    Collections.singletonList(rootTopic.getName()));
        } else {
            wordExporter.writeWords(dbWordProvider.getWordsForLanguage(language, (Set<Topic>) null), true,
                    dbWordProvider.findRootTopics(language).stream().map(Topic::getName).collect(Collectors.toList()));
        }
    }

    public void importWordsFromFile(ActionEvent actionEvent) throws IOException {
        InputStream inputStream = null;
        List<Word> words = new WordImporter() {
            @Override
            protected BufferedReader getBufferedReader() {
                return new BufferedReader(new InputStreamReader(inputStream));
            }
        }.readWords();
        if (!words.isEmpty()) {
            String language = words.get(0).getLanguage();
            if (dbWordProvider.languageFrom().contains(language)) {
//TODO ask for clean DB
            }
        }
    }
}
