package org.leo.dictionary.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.leo.dictionary.PlayService;
import org.leo.dictionary.entity.Word;

public class PlaybackControlsFragmentController {

    @FXML
    private Button playStopButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Label stateLabel;

    private final PlayService playService;

    public PlaybackControlsFragmentController(PlayService playService) {
        this.playService = playService;
    }

    @FXML
    public void initialize() {
        setupListeners();
    }

    private void setupListeners() {
        playStopButton.setOnAction(e -> {
            if (playService.isPlaying()) {
                playService.pause();
            } else {
                playService.play();
            }
        });
        nextButton.setOnAction(e -> playService.next());
        previousButton.setOnAction(e -> playService.previous());

        playService.setUiUpdater((word, index) ->
                Platform.runLater(() -> {
                    stateLabel.setText(Word.formatWord(word));
                    if (playService.isPlaying()) {
                        setPlaying();
                    } else {
                        playStopButton.setText("▶");
                    }
                }));
    }

    public void setPlaying() {
        playStopButton.setText("⏸");
    }

    public void updateLabel(String text) {
        stateLabel.setText(text);
    }
}
