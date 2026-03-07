package org.leo.dictionary.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.leo.dictionary.PlayService;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.DBWordProvider;
import org.leo.dictionary.word.provider.WordProvider;
import org.leo.dictionary.word.provider.WordProviderDelegate;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class WordsListFragmentController {

    @FXML
    private ListView<Word> wordList;
    private final PlayService playService;
    private final WordProvider wordProvider;
    private final DBWordProvider dbWordProvider;
    private Supplier<WordCriteria> criteriaSupplier = WordCriteria::new;
    private Runnable onDataChanged = () -> {
    };
    private MenuItem addWordMenuItem;
    private MenuItem editWordMenuItem;
    private MenuItem deleteWordMenuItem;

    public WordsListFragmentController(PlayService playService, WordProvider wordProvider, DBWordProvider dbWordProvider) {
        this.playService = playService;
        this.wordProvider = wordProvider;
        this.dbWordProvider = dbWordProvider;
    }

    public void init(Supplier<WordCriteria> criteriaSupplier, Runnable onDataChanged) {
        this.criteriaSupplier = Objects.requireNonNull(criteriaSupplier);
        this.onDataChanged = Objects.requireNonNull(onDataChanged);
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
        addWordMenuItem = new MenuItem("Add word");
        addWordMenuItem.setOnAction(e -> addWord());
        editWordMenuItem = new MenuItem("Edit word");
        editWordMenuItem.setOnAction(e -> editSelectedWord());
        deleteWordMenuItem = new MenuItem("Delete word");
        deleteWordMenuItem.setOnAction(e -> deleteSelectedWord());

        menu.getItems().addAll(playFrom, addWordMenuItem, editWordMenuItem, deleteWordMenuItem);
        menu.setOnShowing(event -> refreshDbActionsState());
        wordList.setContextMenu(menu);
    }

    public void updateWords(List<Word> words) {
        wordList.setItems(FXCollections.observableList(words));
        if (!words.isEmpty()) {
            wordList.getSelectionModel().select(0);
        }
    }

    private void refreshDbActionsState() {
        boolean dbActive = isDbActive();
        addWordMenuItem.setVisible(dbActive);
        editWordMenuItem.setVisible(dbActive);
        deleteWordMenuItem.setVisible(dbActive);
        Word selectedWord = wordList.getSelectionModel().getSelectedItem();
        editWordMenuItem.setDisable(!dbActive || selectedWord == null);
        deleteWordMenuItem.setDisable(!dbActive || selectedWord == null);
    }

    private boolean isDbActive() {
        return wordProvider instanceof WordProviderDelegate
                && ((WordProviderDelegate) wordProvider).getDelegate() == dbWordProvider;
    }

    private void addWord() {
        if (!isDbActive()) {
            return;
        }
        createWordDialog(null).showAndWait().ifPresent(word -> {
            dbWordProvider.updateWordFully(word);
            onDataChanged.run();
        });
    }

    private void editSelectedWord() {
        Word selected = wordList.getSelectionModel().getSelectedItem();
        if (selected == null || !isDbActive()) {
            return;
        }
        Word wordForEdit = dbWordProvider.findWord(selected.getId());
        if (wordForEdit == null) {
            wordForEdit = selected;
        }
        createWordDialog(wordForEdit).showAndWait().ifPresent(word -> {
            dbWordProvider.updateWordFully(word);
            onDataChanged.run();
        });
    }

    private void deleteSelectedWord() {
        Word selected = wordList.getSelectionModel().getSelectedItem();
        if (selected == null || !isDbActive()) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete word \"" + Word.formatWord(selected) + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Delete word");
        confirm.initOwner(wordList.getScene().getWindow());
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        dbWordProvider.deleteWord(selected.getId());
        onDataChanged.run();
    }

    private javafx.scene.control.Dialog<Word> createWordDialog(Word original) {
        return new WordEditorDialogFactory(wordList.getScene().getWindow(), dbWordProvider, criteriaSupplier).create(original);
    }
}
