package org.leo.dictionary.controller.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.leo.dictionary.config.entity.Translation;

public class TranslationConfigController {

    @FXML
    private CheckBox activeCheck;
    @FXML
    private CheckBox eachTimeCheck;
    @FXML
    private CheckBox allTranslationsCheck;
    @FXML
    private TextField delayField;

    private Translation config;

    public void init(Translation config) {
        this.config = config;
        bindFields();
    }

    private void bindFields() {
        ConfigUtils.bindCheckBoxToBoolean(activeCheck, config, "active", config.isActive());
        ConfigUtils.bindCheckBoxToBoolean(eachTimeCheck, config, "eachTime", config.isEachTime());
        ConfigUtils.bindCheckBoxToBoolean(allTranslationsCheck, config, "allTranslations", config.isAllTranslations());
        ConfigUtils.bindTextFieldToLong(delayField, config, "delay", config.getDelay());
    }
}
