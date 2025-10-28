package org.leo.dictionary.controller.config;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.leo.dictionary.config.entity.ConfigParent;

import java.util.function.Function;

public class ConfigUtils {

    public static void bindCheckBoxToBoolean(CheckBox checkBox, ConfigParent config, String key, boolean value) {
        checkBox.setSelected(value);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> config.put(key, newVal));
    }

    public static <T> void bindTextField(TextField field, ConfigParent config, String key, T initial, Function<String, T> parser) {
        field.setText(String.valueOf(initial));

        field.textProperty().addListener((obs, oldVal, newVal) -> setValueInConfig(config, key, parser, newVal));

        field.setOnAction(e -> setValueInConfig(config, key, parser, field.getText()));
    }

    private static <T> void setValueInConfig(ConfigParent config, String key, Function<String, T> parser, String newVal) {
        try {
            config.put(key, parser.apply(newVal));
        } catch (Exception ignored) {}
    }

    // Convenience wrappers
    public static void bindTextFieldToInt(TextField field, ConfigParent config, String key, int value) {
        bindTextField(field, config, key, value, Integer::parseInt);
    }

    public static void bindTextFieldToLong(TextField field, ConfigParent config, String key, long value) {
        bindTextField(field, config, key, value, Long::parseLong);
    }

    public static void bindTextFieldToDouble(TextField field, ConfigParent config, String key, double value) {
        bindTextField(field, config, key, value, Double::parseDouble);
    }

    public static void bindTextFieldToString(TextField field, ConfigParent config, String key, String value) {
        bindTextField(field, config, key, value, s -> s);
    }
}
