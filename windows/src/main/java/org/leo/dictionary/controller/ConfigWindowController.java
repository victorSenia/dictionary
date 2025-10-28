package org.leo.dictionary.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.FileConfigurationReader;
import org.leo.dictionary.config.entity.Configuration;
import org.leo.dictionary.controller.config.GeneralConfigController;
import org.leo.dictionary.controller.config.RepeatConfigController;
import org.leo.dictionary.controller.config.SpellingConfigController;
import org.leo.dictionary.controller.config.TranslationConfigController;

public class ConfigWindowController {

    private final ConfigurationService configurationService;
    private final FileConfigurationReader configurationReader;
    @FXML
    private TabPane tabPane;
    @FXML
    private GeneralConfigController generalController;
    @FXML
    private RepeatConfigController repeatController;
    @FXML
    private TranslationConfigController translationController;
    @FXML
    private SpellingConfigController spellingController;

    public ConfigWindowController(ConfigurationService configurationService, FileConfigurationReader configurationReader) {
        this.configurationService = configurationService;
        this.configurationReader = configurationReader;
    }

    @FXML
    public void initialize() {
        Configuration configuration = configurationService.getConfiguration();
        generalController.init(configuration.getGeneral());
        repeatController.init(configuration.getRepeat());
        translationController.init(configuration.getTranslation());
        spellingController.init(configuration.getSpelling());
    }

    public void onClose() {
        configurationReader.updateConfiguration(configurationService.getConfiguration());
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }

}