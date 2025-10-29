package org.leo.dictionary.controller;

import javafx.fxml.FXML;
import org.leo.dictionary.ExternalWordProvider;
import org.leo.dictionary.PlayService;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.util.List;

public class DictionaryController {
    @FXML
    private PlaybackControlsFragmentController playbackControlsFragmentController;
    @FXML
    private TopicsListFragmentController topicsListFragmentController;
    @FXML
    private WordsListFragmentController wordsListFragmentController;
    private final PlayService playService;
    private final ExternalWordProvider wordProvider;

    public DictionaryController(PlayService playService, ExternalWordProvider wordProvider) {
        this.playService = playService;
        this.wordProvider = wordProvider;
    }

    @FXML
    public void initialize() {
        topicsListFragmentController.init(this::onTopicsSelected);
        refreshWords(new WordCriteria());
    }

    private void onTopicsSelected(List<Topic> selectedTopics) {
        WordCriteria criteria = new WordCriteria();
        criteria.setTopicsOr(selectedTopics);
        refreshWords(criteria);
    }

    private void refreshWords(WordCriteria criteria) {
        List<Word> words = wordProvider.findWords(criteria);
        playService.setWords(words);
        wordsListFragmentController.updateWords(words);
        playbackControlsFragmentController.updateLabel("Found " + words.size() + " words");
    }

}
