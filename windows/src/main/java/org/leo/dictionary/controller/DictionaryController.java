package org.leo.dictionary.controller;

import javafx.fxml.FXML;
import org.leo.dictionary.ExternalWordProvider;
import org.leo.dictionary.PlayService;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.util.List;

public class DictionaryController {
    @FXML
    private MenuBarController menuBarController;
    @FXML
    private PlaybackControlsFragmentController playbackControlsFragmentController;
    @FXML
    private FilterSidebarFragmentController filterSidebarFragmentController;
    @FXML
    private WordsListFragmentController wordsListFragmentController;
    private final PlayService playService;
    private final ExternalWordProvider wordProvider;
    private WordCriteria currentCriteria = new WordCriteria();

    public DictionaryController(PlayService playService, ExternalWordProvider wordProvider) {
        this.playService = playService;
        this.wordProvider = wordProvider;
    }

    @FXML
    public void initialize() {
        menuBarController.init(() -> currentCriteria, this::refreshCurrentWords, this::onProviderChanged);
        wordsListFragmentController.init(() -> currentCriteria, this::refreshCurrentWords);
        filterSidebarFragmentController.init(this::refreshWords, () -> currentCriteria, this::onFilterReset);
        resetCriteria();
        refreshWords(currentCriteria);
    }

    private void refreshWords(WordCriteria criteria) {
        applyCriteria(criteria);
        List<Word> words = wordProvider.findWords(criteria);
        playService.setWords(words);
        wordsListFragmentController.updateWords(words);
        playbackControlsFragmentController.updateLabel("Found " + words.size() + " words");
        filterSidebarFragmentController.reloadFromCriteria(currentCriteria);
    }

    private void refreshCurrentWords() {
        refreshWords(currentCriteria);
    }

    private void onProviderChanged() {
        resetCriteria();
        refreshWords(currentCriteria);
    }

    private void onFilterReset() {
        resetCriteria();
        refreshWords(currentCriteria);
    }

    private void applyCriteria(WordCriteria source) {
        if (source == currentCriteria) {
            return;
        }
        currentCriteria.setKnowledgeFrom(source.getKnowledgeFrom());
        currentCriteria.setKnowledgeTo(source.getKnowledgeTo());
        currentCriteria.setLanguageFrom(source.getLanguageFrom());
        currentCriteria.setLanguageTo(source.getLanguageTo());
        currentCriteria.setPlayTranslationFor(source.getPlayTranslationFor());
        currentCriteria.setTopicsOr(source.getTopicsOr());
        currentCriteria.setRootTopics(source.getRootTopics());
        currentCriteria.setNoRootTopic(source.isNoRootTopic());
        currentCriteria.setNoTopic(source.isNoTopic());
        currentCriteria.setShuffleRandom(source.getShuffleRandom());
        currentCriteria.setWordsOrderMode(source.getWordsOrderMode());
    }

    private void resetCriteria() {
        currentCriteria = new WordCriteria();
        String defaultLanguage = wordProvider.languageFrom().stream().findFirst().orElse(null);
        currentCriteria.setLanguageFrom(defaultLanguage);
    }
}
