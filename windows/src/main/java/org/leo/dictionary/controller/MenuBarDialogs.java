package org.leo.dictionary.controller;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.word.provider.DBWordProvider;
import org.leo.dictionary.word.provider.WordProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

final class MenuBarDialogs {

    private MenuBarDialogs() {
    }

    static String chooseLanguageFallback(Window owner, String title, String headerText, DBWordProvider dbWordProvider, WordProvider wordProvider) {
        List<String> languages = new ArrayList<>(dbWordProvider.languageFrom());
        if (languages.isEmpty()) {
            languages = new ArrayList<>(wordProvider.languageFrom());
        }
        if (languages.isEmpty()) {
            ControllerAlerts.showInfo(owner, "No languages available.");
            return null;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(languages.get(0), languages);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.initOwner(owner);
        return dialog.showAndWait().orElse(null);
    }

    static ExportSelection chooseExportSelection(Window owner, DBWordProvider dbWordProvider, WordProvider wordProvider) {
        List<String> languages = new ArrayList<>(dbWordProvider.languageFrom());
        if (languages.isEmpty()) {
            languages = new ArrayList<>(wordProvider.languageFrom());
        }
        if (languages.isEmpty()) {
            ControllerAlerts.showInfo(owner, "No languages available.");
            return null;
        }

        Dialog<ExportSelection> dialog = new Dialog<>();
        dialog.setTitle("Export words");
        dialog.setHeaderText("Choose language and optional root topic");
        dialog.initOwner(owner);
        ButtonType exportButton = new ButtonType("Export", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(exportButton, ButtonType.CANCEL);

        ComboBox<String> languageBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(languages));
        languageBox.setValue(languages.get(0));
        ComboBox<String> rootTopicBox = new ComboBox<>();

        Runnable refreshRootTopics = () -> {
            String selectedLanguage = languageBox.getValue();
            List<String> topicNames = new ArrayList<>();
            topicNames.add("<All topics>");
            if (selectedLanguage != null) {
                topicNames.addAll(dbWordProvider.findRootTopics(selectedLanguage).stream().map(Topic::getName).collect(Collectors.toList()));
            }
            rootTopicBox.getItems().setAll(topicNames);
            rootTopicBox.setValue(topicNames.get(0));
        };
        refreshRootTopics.run();
        languageBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshRootTopics.run());

        VBox content = new VBox(8,
                new Label("Language"),
                languageBox,
                new Label("Root topic"),
                rootTopicBox
        );
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(button -> {
            if (button != exportButton) {
                return null;
            }
            String language = languageBox.getValue();
            if (language == null || language.isBlank()) {
                return null;
            }
            String selectedRoot = rootTopicBox.getValue();
            return new ExportSelection(language, "<All topics>".equals(selectedRoot) ? null : selectedRoot);
        });
        return dialog.showAndWait().orElse(null);
    }

    static KnowledgeControl createKnowledgeControl(double initialValue) {
        Slider slider = new Slider(0.0, 1.0, clampKnowledge(initialValue));
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.1);
        slider.setBlockIncrement(0.01);

        TextField valueField = new TextField(String.format(Locale.US, "%.2f", slider.getValue()));
        valueField.setPrefWidth(70);

        slider.valueProperty().addListener((obs, oldValue, newValue) ->
                valueField.setText(String.format(Locale.US, "%.2f", newValue.doubleValue())));
        valueField.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                slider.setValue(clampKnowledge(Double.parseDouble(newValue.trim())));
            } catch (Exception ignored) {
                // Keep current slider value while user types.
            }
        });

        VBox container = new VBox(4, slider, valueField);
        return new KnowledgeControl(container, slider);
    }

    static double clampKnowledge(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    static final class ExportSelection {
        private final String language;
        private final String rootTopicName;

        ExportSelection(String language, String rootTopicName) {
            this.language = language;
            this.rootTopicName = rootTopicName;
        }

        String getLanguage() {
            return language;
        }

        String getRootTopicName() {
            return rootTopicName;
        }
    }

    static final class KnowledgeControl {
        private final VBox container;
        private final Slider slider;

        KnowledgeControl(VBox container, Slider slider) {
            this.container = Objects.requireNonNull(container);
            this.slider = Objects.requireNonNull(slider);
        }

        VBox getContainer() {
            return container;
        }

        Slider getSlider() {
            return slider;
        }
    }
}
