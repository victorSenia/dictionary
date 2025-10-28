package org.leo.dictionary.controller.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.leo.dictionary.config.entity.General;

public class GeneralConfigController {

    @FXML
    private TextField knowledgeIncreaseField;
    @FXML
    private TextField delayBeforeField;
    @FXML
    private TextField delayPerLetterAfterField;
    @FXML
    private CheckBox includeKnownCheck;
    @FXML
    private CheckBox includeArticleCheck;

    private General config;

    public void init(General config) {
        this.config = config;
        bindFields();
    }

    private void bindFields() {
        ConfigUtils.bindTextFieldToDouble(knowledgeIncreaseField, config, "knowledgeIncrease", config.getKnowledgeIncrease());
        ConfigUtils.bindTextFieldToLong(delayBeforeField, config, "delayBefore", config.getDelayBefore());
        ConfigUtils.bindTextFieldToLong(delayPerLetterAfterField, config, "delayPerLetterAfter", config.getDelayPerLetterAfter());
        ConfigUtils.bindCheckBoxToBoolean(includeKnownCheck, config, "includeKnown", config.isIncludeKnown());
        ConfigUtils.bindCheckBoxToBoolean(includeArticleCheck, config, "includeArticle", config.isIncludeArticle());
    }

}
