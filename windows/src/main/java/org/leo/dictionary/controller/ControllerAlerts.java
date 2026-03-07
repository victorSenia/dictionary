package org.leo.dictionary.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

final class ControllerAlerts {

    private ControllerAlerts() {
    }

    static void showInfo(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    static void showError(Window owner, String header, Throwable throwable) {
        String details = throwable == null ? "" : (throwable.getMessage() == null ? throwable.toString() : throwable.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR, details, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    static void showValidationError(Window owner, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
