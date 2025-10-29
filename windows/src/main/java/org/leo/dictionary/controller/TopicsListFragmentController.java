package org.leo.dictionary.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.word.provider.WordProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TopicsListFragmentController {

    @FXML
    private ListView<Topic> topicsList;

    private final WordProvider wordProvider;
    private Consumer<List<Topic>> onTopicsSelected;
    public TopicsListFragmentController(WordProvider wordProvider) {
        this.wordProvider = wordProvider;}

    public void init(Consumer<List<Topic>> onTopicsSelected) {
        this.onTopicsSelected = onTopicsSelected;

        topicsList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Topic item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
//TODO correct language
        topicsList.setItems(FXCollections.observableList(wordProvider.findTopics("de_DE", 2)));
        topicsList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        addContextMenu();
    }

    private void addContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem orTopics = new MenuItem("Select any of topics");
        orTopics.setOnAction(e -> {
            List<Topic> selected = new ArrayList<>(topicsList.getSelectionModel().getSelectedItems());
            onTopicsSelected.accept(selected);
        });
        menu.getItems().add(orTopics);
        topicsList.setContextMenu(menu);
    }
}
