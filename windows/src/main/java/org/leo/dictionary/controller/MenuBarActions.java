package org.leo.dictionary.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.leo.dictionary.WindowsModule;
import org.leo.dictionary.config.entity.ParseWords;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.DBWordProvider;
import org.leo.dictionary.word.provider.WordExporter;
import org.leo.dictionary.word.provider.WordImporter;
import org.leo.dictionary.word.provider.WordProvider;
import org.leo.dictionary.word.provider.WordProviderDelegate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class MenuBarActions {
    private final WordProvider wordProvider;
    private final DBWordProvider dbWordProvider;
    private final Supplier<Window> ownerSupplier;
    private final FileChooserDirectoryMemory directoryMemory = new FileChooserDirectoryMemory();
    private Supplier<WordCriteria> criteriaSupplier;
    private Runnable onDataChanged;
    private Runnable onProviderChanged;
    private MenuItem useDbMenuItem;

    MenuBarActions(WordProvider wordProvider, DBWordProvider dbWordProvider, Supplier<Window> ownerSupplier) {
        this.wordProvider = wordProvider;
        this.dbWordProvider = dbWordProvider;
        this.ownerSupplier = ownerSupplier;
    }

    void bindUseDbMenuItem(MenuItem useDbMenuItem) {
        this.useDbMenuItem = useDbMenuItem;
        refreshProviderActions();
    }

    void init(Supplier<WordCriteria> criteriaSupplier, Runnable onDataChanged, Runnable onProviderChanged) {
        this.criteriaSupplier = Objects.requireNonNull(criteriaSupplier);
        this.onDataChanged = Objects.requireNonNull(onDataChanged);
        this.onProviderChanged = Objects.requireNonNull(onProviderChanged);
        refreshProviderActions();
    }

    void parseWords() {
        WordProviderDelegate delegate = getWordProviderDelegate();
        if (delegate == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose words source file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt", "*.csv", "*.tsv", "*.dat", "*.*"));
        directoryMemory.configureStartDirectory(chooser);
        File selectedFile = chooser.showOpenDialog(ownerWindow());
        if (selectedFile == null) {
            return;
        }
        directoryMemory.remember(selectedFile);

        ParseWords configuration = WindowsModule.getDefaultParseWordsConfig();
        configuration.setPath(selectedFile.getAbsolutePath());
        delegate.setWordProvider(WindowsModule.createFileWordProvider(configuration));
        refreshProviderActions();
        onProviderChanged.run();
    }

    void useDb() {
        WordProviderDelegate delegate = getWordProviderDelegate();
        if (delegate == null) {
            return;
        }
        delegate.setWordProvider(dbWordProvider);
        refreshProviderActions();
        onProviderChanged.run();
    }

    void importWords() {
        WordProviderDelegate delegate = getWordProviderDelegate();
        if (delegate == null) {
            return;
        }
        if (delegate.getDelegate() == dbWordProvider) {
            ControllerAlerts.showInfo(ownerWindow(), "Current provider is already database provider.");
            return;
        }
        WordCriteria criteria = copyCriteria(getCurrentCriteria());
        BackgroundTaskRunner.runInBackground("dictionary-import-words", () -> {
            try {
                dbWordProvider.importWords(wordProvider.findWords(criteria));
                Platform.runLater(() -> {
                    onDataChanged.run();
                    ControllerAlerts.showInfo(ownerWindow(), "Import finished.");
                });
            } catch (RuntimeException ex) {
                Platform.runLater(() -> ControllerAlerts.showError(ownerWindow(), "Import failed", ex));
            }
        }, ex -> Platform.runLater(() -> ControllerAlerts.showError(ownerWindow(), "Operation failed", ex)));
    }

    void cleanDb() {
        String language = getCurrentCriteria().getLanguageFrom();
        if (language == null || language.isBlank()) {
            language = MenuBarDialogs.chooseLanguageFallback(ownerWindow(), "Clean DB", "Choose language to delete", dbWordProvider, wordProvider);
            if (language == null) {
                return;
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete all words for " + language + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm database cleanup");
        confirm.initOwner(ownerWindow());
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        String finalLanguage = language;
        runInBackground("dictionary-clean-db", () -> {
            dbWordProvider.deleteWords(finalLanguage);
            Platform.runLater(() -> {
                onDataChanged.run();
                ControllerAlerts.showInfo(ownerWindow(), "DB cleaned for " + finalLanguage);
            });
        });
    }

    void setKnowledge() {
        WordProviderDelegate delegate = getWordProviderDelegate();
        if (delegate == null) {
            return;
        }
        if (delegate.getDelegate() != dbWordProvider) {
            ControllerAlerts.showInfo(ownerWindow(), "Set knowledge is available only when database source is active.");
            return;
        }
        Dialog<Double> knowledgeDialog = new Dialog<>();
        knowledgeDialog.setTitle("Set knowledge");
        knowledgeDialog.setHeaderText("Set knowledge for currently filtered words (0.0 - 1.0)");
        knowledgeDialog.initOwner(ownerWindow());
        ButtonType applyButton = new ButtonType("Apply", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        knowledgeDialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);

        MenuBarDialogs.KnowledgeControl knowledgeControl = MenuBarDialogs.createKnowledgeControl(0.0);
        VBox content = new VBox(8, new Label("Knowledge value"), knowledgeControl.getContainer());
        knowledgeDialog.getDialogPane().setContent(content);
        knowledgeDialog.setResultConverter(buttonType -> buttonType == applyButton ? knowledgeControl.getSlider().getValue() : null);

        Optional<Double> enteredValue = knowledgeDialog.showAndWait();
        if (enteredValue.isEmpty()) {
            return;
        }
        double knowledge = enteredValue.get();
        if (knowledge < 0.0 || knowledge > 1.0) {
            ControllerAlerts.showError(ownerWindow(), "Invalid knowledge value", new IllegalArgumentException("Knowledge must be in range 0.0 - 1.0"));
            return;
        }
        WordCriteria criteriaSnapshot = copyCriteria(getCurrentCriteria());
        runInBackground("dictionary-set-knowledge", () -> {
            List<Word> words = wordProvider.findWords(criteriaSnapshot);
            for (Word word : words) {
                word.setKnowledge(knowledge);
            }
            dbWordProvider.updateWord(words);
            Platform.runLater(() -> {
                onDataChanged.run();
                ControllerAlerts.showInfo(ownerWindow(), "Knowledge updated for " + words.size() + " words.");
            });
        });
    }

    void exportWordsToFile() {
        MenuBarDialogs.ExportSelection selection = MenuBarDialogs.chooseExportSelection(ownerWindow(), dbWordProvider, wordProvider);
        if (selection == null) {
            return;
        }
        String language = selection.getLanguage();
        List<Topic> rootTopics = dbWordProvider.findRootTopics(language);
        Optional<Topic> selectedRoot = selection.getRootTopicName() == null
                ? Optional.empty()
                : rootTopics.stream().filter(topic -> Objects.equals(topic.getName(), selection.getRootTopicName())).findFirst();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save exported words");
        chooser.setInitialFileName("words_" + language + ".txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        directoryMemory.configureStartDirectory(chooser);
        File targetFile = chooser.showSaveDialog(ownerWindow());
        if (targetFile == null) {
            return;
        }
        directoryMemory.remember(targetFile);

        runInBackground("dictionary-export-words", () -> {
            try (OutputStream outputStream = new FileOutputStream(targetFile)) {
                WordExporter wordExporter = new WordExporter() {
                    @Override
                    protected BufferedWriter getBufferedWriter() {
                        return new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    }
                };
                if (selectedRoot.isPresent()) {
                    Topic rootTopic = selectedRoot.get();
                    wordExporter.writeWords(dbWordProvider.getWordsForLanguage(language, rootTopic), false, Collections.singletonList(rootTopic.getName()));
                } else {
                    wordExporter.writeWords(dbWordProvider.getWordsForLanguage(language, (java.util.Set<Topic>) null), true,
                            rootTopics.stream().map(Topic::getName).collect(Collectors.toList()));
                }
                Platform.runLater(() -> ControllerAlerts.showInfo(ownerWindow(), "Export finished: " + targetFile.getAbsolutePath()));
            } catch (IOException ex) {
                Platform.runLater(() -> ControllerAlerts.showError(ownerWindow(), "Export failed", ex));
            }
        });
    }

    void importWordsFromFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose file to import");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt", "*.csv", "*.tsv", "*.dat", "*.*"));
        directoryMemory.configureStartDirectory(chooser);
        File sourceFile = chooser.showOpenDialog(ownerWindow());
        if (sourceFile == null) {
            return;
        }
        directoryMemory.remember(sourceFile);
        runInBackground("dictionary-import-file-read", () -> {
            List<Word> words;
            try (InputStream inputStream = new FileInputStream(sourceFile)) {
                words = new WordImporter() {
                    @Override
                    protected BufferedReader getBufferedReader() {
                        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    }
                }.readWords();
            } catch (IOException ex) {
                Platform.runLater(() -> ControllerAlerts.showError(ownerWindow(), "Import failed", ex));
                return;
            }
            if (words.isEmpty()) {
                Platform.runLater(() -> ControllerAlerts.showInfo(ownerWindow(), "No words found in file."));
                return;
            }

            String language = words.get(0).getLanguage();
            Platform.runLater(() -> {
                boolean cleanFirst = false;
                if (language != null && dbWordProvider.languageFrom().contains(language)) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Language " + language + " already exists in DB. Clean current words before import?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText("Existing language data found");
                    confirm.initOwner(ownerWindow());
                    cleanFirst = confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
                }
                boolean finalCleanFirst = cleanFirst;
                runInBackground("dictionary-import-file-db", () -> {
                    if (finalCleanFirst && language != null) {
                        dbWordProvider.deleteWords(language);
                    }
                    dbWordProvider.importWords(words);
                    Platform.runLater(() -> {
                        onDataChanged.run();
                        ControllerAlerts.showInfo(ownerWindow(), "Import finished.");
                    });
                });
            });
        });
    }

    private WordCriteria getCurrentCriteria() {
        if (criteriaSupplier == null) {
            throw new IllegalStateException("MenuBarController is not initialized");
        }
        return criteriaSupplier.get();
    }

    private static WordCriteria copyCriteria(WordCriteria source) {
        WordCriteria copy = new WordCriteria();
        copy.setKnowledgeFrom(source.getKnowledgeFrom());
        copy.setKnowledgeTo(source.getKnowledgeTo());
        copy.setTopicsOr(source.getTopicsOr() == null ? null : new HashSet<>(source.getTopicsOr()));
        copy.setRootTopics(source.getRootTopics() == null ? null : new HashSet<>(source.getRootTopics()));
        copy.setLanguageFrom(source.getLanguageFrom());
        copy.setLanguageTo(source.getLanguageTo() == null ? null : new HashSet<>(source.getLanguageTo()));
        copy.setPlayTranslationFor(source.getPlayTranslationFor() == null ? null : new HashSet<>(source.getPlayTranslationFor()));
        copy.setNoRootTopic(source.isNoRootTopic());
        copy.setNoTopic(source.isNoTopic());
        copy.setShuffleRandom(source.getShuffleRandom());
        copy.setWordsOrderMode(source.getWordsOrderMode());
        return copy;
    }

    private WordProviderDelegate getWordProviderDelegate() {
        WordProviderDelegate delegate = tryGetWordProviderDelegate();
        if (delegate != null) {
            return delegate;
        }
        ControllerAlerts.showError(ownerWindow(), "Word provider does not support switching data source", null);
        return null;
    }

    private WordProviderDelegate tryGetWordProviderDelegate() {
        if (wordProvider instanceof WordProviderDelegate) {
            return (WordProviderDelegate) wordProvider;
        }
        return null;
    }

    private void refreshProviderActions() {
        if (useDbMenuItem == null) {
            return;
        }
        WordProviderDelegate delegate = tryGetWordProviderDelegate();
        useDbMenuItem.setDisable(delegate != null && delegate.getDelegate() == dbWordProvider);
    }

    private void runInBackground(String threadName, Runnable work) {
        BackgroundTaskRunner.runInBackground(threadName, work,
                ex -> Platform.runLater(() -> ControllerAlerts.showError(ownerWindow(), "Operation failed", ex)));
    }

    private Window ownerWindow() {
        return ownerSupplier.get();
    }
}
