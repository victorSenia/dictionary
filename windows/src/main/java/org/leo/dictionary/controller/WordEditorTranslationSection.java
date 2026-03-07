package org.leo.dictionary.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.WordProvider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class WordEditorTranslationSection {
    private final WordProvider wordProvider;
    private final WordCriteria criteria;
    private final Word original;
    private final ComboBox<String> languageFrom;
    private final VBox translationBox = new VBox(6);
    private final List<TranslationRow> translationRows = new ArrayList<>();
    private final Button addTranslationButton = new Button("Add translation language");

    WordEditorTranslationSection(WordProvider wordProvider, WordCriteria criteria, Word original, ComboBox<String> languageFrom) {
        this.wordProvider = wordProvider;
        this.criteria = criteria;
        this.original = original;
        this.languageFrom = languageFrom;
        addTranslationButton.setOnAction(e ->
                addTranslationRow(currentTargetLanguages(), "", ""));
        addTranslationButton.setMaxWidth(Double.MAX_VALUE);
        rebuild();
    }

    void onLanguageChanged() {
        rebuild();
    }

    VBox getPanel() {
        return new VBox(6, translationBox, addTranslationButton);
    }

    List<Translation> buildTranslations() {
        HashMap<String, ArrayDeque<Long>> existingIds = new HashMap<>();
        if (original != null && original.getTranslations() != null) {
            for (Translation translation : original.getTranslations()) {
                existingIds.computeIfAbsent(translationKey(translation.getLanguage(), translation.getTranslation()),
                                k -> new ArrayDeque<>())
                        .add(translation.getId());
            }
        }
        List<Translation> result = new ArrayList<>();
        for (TranslationRow row : translationRows) {
            String language = nullToEmpty(row.language.getEditor().getText()).trim();
            String rawValue = nullToEmpty(row.value.getText()).trim();
            if (language.isEmpty() || rawValue.isEmpty()) {
                continue;
            }
            for (String value : rawValue.split(";")) {
                String normalized = value.trim();
                if (!normalized.isEmpty()) {
                    Translation translation = new Translation();
                    translation.setLanguage(language);
                    translation.setTranslation(normalized);
                    ArrayDeque<Long> ids = existingIds.get(translationKey(language, normalized));
                    if (ids != null && !ids.isEmpty()) {
                        translation.setId(ids.removeFirst());
                    }
                    result.add(translation);
                }
            }
        }
        return result;
    }

    private void rebuild() {
        translationRows.clear();
        translationBox.getChildren().clear();
        List<String> languages = currentTargetLanguages();
        for (String language : languages) {
            addTranslationRow(languages, language, originalTranslationForLanguage(original, language));
        }
    }

    private List<String> currentTargetLanguages() {
        String selectedLanguage = languageFrom.getEditor().getText().trim();
        List<String> targetLanguages = selectedLanguage.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(wordProvider.languageTo(selectedLanguage));
        if (criteria.getLanguageTo() != null && !criteria.getLanguageTo().isEmpty()) {
            targetLanguages.addAll(criteria.getLanguageTo());
        }
        if (original != null && original.getTranslations() != null) {
            targetLanguages.addAll(original.getTranslations().stream().map(Translation::getLanguage).collect(Collectors.toList()));
        }
        return targetLanguages.stream().distinct().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    private void addTranslationRow(List<String> languageOptions, String language, String translationValue) {
        TranslationRow row = new TranslationRow();
        row.language = new ComboBox<>(FXCollections.observableArrayList(languageOptions));
        row.language.setEditable(true);
        row.language.setValue(language);
        row.language.setPrefWidth(180);
        row.language.setMaxWidth(Double.MAX_VALUE);
        row.value = new TextField(translationValue);
        row.value.setMaxWidth(Double.MAX_VALUE);
        row.removeButton = new Button("x");
        row.container = new HBox(8, new Label("Lang"), row.language, new Label("Value"), row.value, row.removeButton);
        HBox.setHgrow(row.language, Priority.SOMETIMES);
        HBox.setHgrow(row.value, Priority.ALWAYS);
        row.removeButton.setOnAction(e -> {
            translationRows.remove(row);
            translationBox.getChildren().remove(row.container);
        });
        translationRows.add(row);
        translationBox.getChildren().add(row.container);
    }

    private static String translationKey(String language, String translation) {
        return language + "\u0000" + translation;
    }

    private static String originalTranslationForLanguage(Word original, String language) {
        if (original == null || original.getTranslations() == null) {
            return "";
        }
        return original.getTranslations().stream()
                .filter(t -> Objects.equals(language, t.getLanguage()))
                .map(Translation::getTranslation)
                .collect(Collectors.joining("; "));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class TranslationRow {
        private HBox container;
        private ComboBox<String> language;
        private TextField value;
        private Button removeButton;
    }
}
