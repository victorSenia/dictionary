package org.leo.dictionary.controller;

import javafx.stage.FileChooser;

import java.io.File;

final class FileChooserDirectoryMemory {
    private File lastUsedDirectory;

    void configureStartDirectory(FileChooser chooser) {
        File startDirectory = getStartDirectory();
        if (startDirectory != null) {
            chooser.setInitialDirectory(startDirectory);
        }
    }

    void remember(File selectedFile) {
        File directory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
        if (directory != null && directory.isDirectory()) {
            lastUsedDirectory = directory;
        }
    }

    private File getStartDirectory() {
        if (lastUsedDirectory != null && lastUsedDirectory.isDirectory()) {
            return lastUsedDirectory;
        }
        File currentDirectory = new File(System.getProperty("user.dir"));
        if (currentDirectory.isDirectory()) {
            return currentDirectory;
        }
        return null;
    }
}
