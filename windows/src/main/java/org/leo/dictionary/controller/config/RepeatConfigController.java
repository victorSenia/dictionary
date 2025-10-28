package org.leo.dictionary.controller.config;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.leo.dictionary.config.entity.Repeat;

public class RepeatConfigController {

    @FXML
    private TextField timesField;
    @FXML
    private TextField delayField;

    private Repeat config;

    public void init(Repeat config) {
        this.config = config;
        bindFields();
    }

    private void bindFields() {
        ConfigUtils.bindTextFieldToInt(timesField, config, "times", config.getTimes());
        ConfigUtils.bindTextFieldToLong(delayField, config, "delay", config.getDelay());
    }
}
