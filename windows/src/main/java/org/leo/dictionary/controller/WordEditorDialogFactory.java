package org.leo.dictionary.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.DBWordProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

final class WordEditorDialogFactory {
    private final Window owner;
    private final DBWordProvider dbWordProvider;
    private final Supplier<WordCriteria> criteriaSupplier;

    WordEditorDialogFactory(Window owner, DBWordProvider dbWordProvider, Supplier<WordCriteria> criteriaSupplier) {
        this.owner = owner;
        this.dbWordProvider = dbWordProvider;
        this.criteriaSupplier = criteriaSupplier;
    }

    Dialog<Word> create(Word original) {
        Dialog<Word> dialog = createDialogShell(original);
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        WordCriteria criteria = criteriaSupplier.get();
        ComboBox<String> languageFrom = createLanguageBox(criteria, original);
        TextField articleField = new TextField(original == null ? "" : nullToEmpty(original.getArticle()));
        TextField wordField = new TextField(original == null ? "" : nullToEmpty(original.getWord()));
        TextField additionalInfoField = new TextField(original == null ? "" : nullToEmpty(original.getAdditionalInformation()));
        configureTextInputs(articleField, wordField, additionalInfoField);
        Slider knowledgeSlider = new Slider(0.0, 1.0, original == null ? 0.0 : clampKnowledge(original.getKnowledge()));
        knowledgeSlider.setShowTickMarks(true);
        knowledgeSlider.setShowTickLabels(true);
        knowledgeSlider.setMajorTickUnit(0.2);
        Label knowledgeLabel = new Label(formatKnowledge(knowledgeSlider.getValue()));
        knowledgeSlider.valueProperty().addListener((obs, oldValue, newValue) -> knowledgeLabel.setText(formatKnowledge(newValue.doubleValue())));

        WordEditorTranslationSection translationsSection = new WordEditorTranslationSection(dbWordProvider, criteria, original, languageFrom);
        WordEditorTopicsSection topicsSection = new WordEditorTopicsSection(dbWordProvider, criteria, original, languageFrom);

        languageFrom.valueProperty().addListener((obs, oldValue, newValue) -> {
            translationsSection.onLanguageChanged();
            topicsSection.onLanguageChanged();
        });
        languageFrom.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            translationsSection.onLanguageChanged();
            topicsSection.onLanguageChanged();
        });

        GridPane grid = buildGrid(languageFrom, articleField, wordField, additionalInfoField,
                new VBox(4, knowledgeSlider, knowledgeLabel), translationsSection.getPanel(), topicsSection.getPanel());
        ScrollPane pane = new ScrollPane(grid);
        pane.setFitToWidth(true);
        pane.setPrefViewportHeight(640);
        pane.setPrefViewportWidth(920);
        dialog.getDialogPane().setContent(pane);

        dialog.setResultConverter(button -> {
            if (button != saveButton) {
                return null;
            }
            String language = nullToEmpty(languageFrom.getEditor().getText()).trim();
            String wordValue = nullToEmpty(wordField.getText()).trim();
            if (language.isEmpty() || wordValue.isEmpty()) {
                ControllerAlerts.showValidationError(owner, "Invalid word data", "Language and word are required.");
                return null;
            }
            Word result = new Word();
            if (original != null) {
                result.setId(original.getId());
            }
            result.setTopics(topicsSection.selectedTopics());
            result.setLanguage(language);
            result.setArticle(blankToNull(articleField.getText()));
            result.setWord(wordValue);
            result.setAdditionalInformation(blankToNull(additionalInfoField.getText()));
            result.setKnowledge(clampKnowledge(knowledgeSlider.getValue()));
            result.setTranslations(translationsSection.buildTranslations());
            return result;
        });
        return dialog;
    }

    private Dialog<Word> createDialogShell(Word original) {
        Dialog<Word> dialog = new Dialog<>();
        dialog.setTitle(original == null ? "Add word" : "Edit word");
        dialog.setHeaderText(original == null ? "Create new word" : "Edit selected word");
        dialog.initOwner(owner);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(900);
        dialog.setOnShown(event -> {
            if (dialog.getDialogPane().getScene() != null && dialog.getDialogPane().getScene().getWindow() != null) {
                dialog.getDialogPane().getScene().getWindow().setWidth(980);
                dialog.getDialogPane().getScene().getWindow().setHeight(760);
            }
        });
        return dialog;
    }

    private ComboBox<String> createLanguageBox(WordCriteria criteria, Word original) {
        List<String> sourceLanguages = new ArrayList<>(dbWordProvider.languageFrom());
        String defaultLanguage = original != null ? original.getLanguage() : criteria.getLanguageFrom();
        if (defaultLanguage != null && !sourceLanguages.contains(defaultLanguage)) {
            sourceLanguages.add(defaultLanguage);
        }
        if (sourceLanguages.isEmpty()) {
            sourceLanguages.add("de_DE");
        }
        ComboBox<String> languageFrom = new ComboBox<>(FXCollections.observableArrayList(sourceLanguages));
        languageFrom.setEditable(true);
        languageFrom.setValue(defaultLanguage != null ? defaultLanguage : sourceLanguages.get(0));
        languageFrom.setMaxWidth(Double.MAX_VALUE);
        if (original != null) {
            languageFrom.setDisable(true);
        }
        return languageFrom;
    }

    private static void configureTextInputs(TextField articleField, TextField wordField, TextField additionalInfoField) {
        articleField.setMaxWidth(Double.MAX_VALUE);
        wordField.setMaxWidth(Double.MAX_VALUE);
        additionalInfoField.setMaxWidth(Double.MAX_VALUE);
    }

    private static GridPane buildGrid(ComboBox<String> languageFrom, TextField articleField, TextField wordField, TextField additionalInfoField,
                                      VBox knowledgeBox, VBox translationsPanel, VBox topicsPanel) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(120);
        labelCol.setPrefWidth(140);
        ColumnConstraints valueCol = new ColumnConstraints();
        valueCol.setHgrow(Priority.ALWAYS);
        valueCol.setFillWidth(true);
        grid.getColumnConstraints().setAll(labelCol, valueCol);

        grid.addRow(0, new Label("Language"), languageFrom);
        grid.addRow(1, new Label("Article"), articleField);
        grid.addRow(2, new Label("Word"), wordField);
        grid.addRow(3, new Label("Additional"), additionalInfoField);
        grid.addRow(4, new Label("Knowledge"), knowledgeBox);
        grid.addRow(5, new Label("Translations"), translationsPanel);
        grid.addRow(6, new Label("Topics"), topicsPanel);
        GridPane.setHgrow(languageFrom, Priority.ALWAYS);
        GridPane.setHgrow(articleField, Priority.ALWAYS);
        GridPane.setHgrow(wordField, Priority.ALWAYS);
        GridPane.setHgrow(additionalInfoField, Priority.ALWAYS);
        GridPane.setHgrow(knowledgeBox, Priority.ALWAYS);
        GridPane.setHgrow(translationsPanel, Priority.ALWAYS);
        GridPane.setHgrow(topicsPanel, Priority.ALWAYS);
        return grid;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        String normalized = nullToEmpty(value).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static double clampKnowledge(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static String formatKnowledge(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
