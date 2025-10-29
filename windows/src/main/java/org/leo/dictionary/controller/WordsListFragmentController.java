package org.leo.dictionary.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.leo.dictionary.PlayService;
import org.leo.dictionary.entity.Word;

import java.util.List;

public class WordsListFragmentController {

    @FXML
    private ListView<Word> wordList;
    private final PlayService playService;

    public WordsListFragmentController(PlayService playService) {
        this.playService = playService;
    }

    @FXML
    public void initialize() {
        wordList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Word item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : Word.formatWord(item));
            }
        });

        addContextMenu();
    }

    private void addContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem playFrom = new MenuItem("Play from");
        playFrom.setOnAction(e -> playService.playFrom(wordList.getSelectionModel().getSelectedIndex()));
        menu.getItems().add(playFrom);
        wordList.setContextMenu(menu);
    }

    public void updateWords(List<Word> words) {
        wordList.setItems(FXCollections.observableList(words));
        if (!words.isEmpty()) {
            wordList.getSelectionModel().select(0);
        }
    }
}
