package org.leo.dictionary.controller;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.DBWordProvider;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class WordEditorTopicsSection {
    private final DBWordProvider dbWordProvider;
    private final ComboBox<String> languageFrom;
    private final ListView<Topic> selectedTopicsList = new ListView<>();
    private final ListView<Topic> availableTopicsList = new ListView<>();
    private final ComboBox<TopicOption> rootTopicCombo = new ComboBox<>();
    private final TextField newTopicField = new TextField();
    private final VBox panel;

    WordEditorTopicsSection(DBWordProvider dbWordProvider, WordCriteria criteria, Word original, ComboBox<String> languageFrom) {
        this.dbWordProvider = dbWordProvider;
        this.languageFrom = languageFrom;
        configureLists();
        configureTopicCreation();
        preloadSelectedTopics(criteria, original);
        refreshForLanguage();
        selectedTopicsList.getItems().addListener((javafx.collections.ListChangeListener<Topic>) change -> refreshForLanguage());
        panel = createPanel();
    }

    void onLanguageChanged() {
        refreshForLanguage();
    }

    VBox getPanel() {
        return panel;
    }

    List<Topic> selectedTopics() {
        return new ArrayList<>(selectedTopicsList.getItems());
    }

    private void configureLists() {
        selectedTopicsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedTopicsList.setPrefHeight(120);
        selectedTopicsList.setMaxWidth(Double.MAX_VALUE);
        selectedTopicsList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Topic item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        availableTopicsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableTopicsList.setPrefHeight(120);
        availableTopicsList.setMaxWidth(Double.MAX_VALUE);
        availableTopicsList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Topic item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    private void configureTopicCreation() {
        newTopicField.setPromptText("New topic name");
        newTopicField.setMaxWidth(Double.MAX_VALUE);
        rootTopicCombo.setMaxWidth(Double.MAX_VALUE);
        rootTopicCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TopicOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name);
            }
        });
        rootTopicCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TopicOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name);
            }
        });
    }

    private VBox createPanel() {
        Button addTopicButton = new Button("Add selected topics");
        addTopicButton.setOnAction(e -> {
            Set<Topic> combined = new LinkedHashSet<>(selectedTopicsList.getItems());
            combined.addAll(availableTopicsList.getSelectionModel().getSelectedItems());
            selectedTopicsList.getItems().setAll(combined);
        });
        Button removeTopicButton = new Button("Remove selected topics");
        removeTopicButton.setOnAction(e -> {
            List<Topic> remaining = new ArrayList<>(selectedTopicsList.getItems());
            remaining.removeAll(selectedTopicsList.getSelectionModel().getSelectedItems());
            selectedTopicsList.getItems().setAll(remaining);
        });
        Button createTopicButton = new Button("Create topic");
        createTopicButton.setOnAction(e -> createTopic());

        HBox createRow = new HBox(8, newTopicField, rootTopicCombo, createTopicButton);
        HBox.setHgrow(newTopicField, Priority.ALWAYS);
        HBox.setHgrow(rootTopicCombo, Priority.ALWAYS);
        return new VBox(6,
                new Label("Selected topics"), selectedTopicsList,
                new HBox(8, addTopicButton, removeTopicButton),
                new Label("Available topics for language"), availableTopicsList,
                createRow);
    }

    private void preloadSelectedTopics(WordCriteria criteria, Word original) {
        if (original != null && original.getTopics() != null) {
            selectedTopicsList.getItems().setAll(original.getTopics());
        } else if (criteria.getTopicsOr() != null && !criteria.getTopicsOr().isEmpty()) {
            selectedTopicsList.getItems().setAll(criteria.getTopicsOr());
        }
    }

    private void createTopic() {
        String topicName = nullToEmpty(newTopicField.getText()).trim();
        String topicLanguage = nullToEmpty(languageFrom.getEditor().getText()).trim();
        if (topicName.isEmpty() || topicLanguage.isEmpty()) {
            return;
        }
        TopicOption selectedRoot = rootTopicCombo.getValue();
        Topic created = new Topic();
        created.setLanguage(topicLanguage);
        created.setName(topicName);
        created.setLevel(selectedRoot == null || selectedRoot.topic == null ? 1 : 2);
        created.setRoot(selectedRoot == null ? null : selectedRoot.topic);

        Set<Topic> combined = new LinkedHashSet<>(selectedTopicsList.getItems());
        combined.add(created);
        selectedTopicsList.getItems().setAll(combined);
        newTopicField.clear();
    }

    private void refreshForLanguage() {
        String selectedLanguage = nullToEmpty(languageFrom.getEditor().getText()).trim();
        if (selectedLanguage.isEmpty()) {
            availableTopicsList.getItems().clear();
            rootTopicCombo.getItems().clear();
            return;
        }
        List<Topic> rootTopics = dbWordProvider.findTopics(selectedLanguage, 1);
        List<Topic> allTopics = dbWordProvider.findTopics(selectedLanguage, 2);
        Set<Long> selectedTopicIds = selectedTopicsList.getItems().stream().map(Topic::getId).collect(Collectors.toSet());
        List<Topic> availableOnly = allTopics.stream()
                .filter(topic -> topic.getId() == 0 || !selectedTopicIds.contains(topic.getId()))
                .collect(Collectors.toList());
        rootTopicCombo.getItems().setAll(buildRootOptions(rootTopics));
        if (!rootTopicCombo.getItems().isEmpty() && rootTopicCombo.getValue() == null) {
            rootTopicCombo.setValue(rootTopicCombo.getItems().get(0));
        }
        availableTopicsList.getItems().setAll(availableOnly);
    }

    private List<TopicOption> buildRootOptions(List<Topic> rootTopics) {
        List<TopicOption> options = new ArrayList<>();
        options.add(new TopicOption("No root", null));
        for (Topic rootTopic : rootTopics) {
            options.add(new TopicOption(rootTopic.getName(), rootTopic));
        }
        return options;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class TopicOption {
        private final String name;
        private final Topic topic;

        private TopicOption(String name, Topic topic) {
            this.name = name;
            this.topic = topic;
        }
    }
}
