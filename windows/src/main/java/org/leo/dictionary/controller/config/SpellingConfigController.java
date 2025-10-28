package org.leo.dictionary.controller.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.leo.dictionary.config.entity.Spelling;

public class SpellingConfigController {

    @FXML
    private CheckBox activeCheck;
    @FXML
    private CheckBox eachTimeCheck;
    @FXML
    private TextField delayField;
    @FXML
    private TextField letterDelayField;

    private Spelling config;

    public void init(Spelling config) {
        this.config = config;
        bindFields();
    }

    private void bindFields() {
        ConfigUtils.bindCheckBoxToBoolean(activeCheck, config, "active", config.isActive());
        ConfigUtils.bindCheckBoxToBoolean(eachTimeCheck, config, "eachTime", config.isEachTime());
        ConfigUtils.bindTextFieldToLong(delayField, config, "delay", config.getDelay());
        ConfigUtils.bindTextFieldToLong(letterDelayField, config, "letterDelay", config.getLetterDelay());
    }
}
