package org.leo.dictionary.audio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class WindowsAudioService implements AudioService {
    private static final Logger LOGGER = Logger.getLogger(WindowsAudioService.class.getName());
    private Map<String, List<String>> voices;

    private Process powerShellProcess = null;

    private static String getCulturesCommand() {
        return "Add-Type -AssemblyName System.Speech; $speechSynthesizer=(New-Object System.Speech.Synthesis.SpeechSynthesizer); " +
                "$speechSynthesizer.GetInstalledVoices().VoiceInfo.Culture.Name;";
    }

    private static String getVoicesCommand(String culture) {
        return "Add-Type -AssemblyName System.Speech; $speechSynthesizer=(New-Object System.Speech.Synthesis.SpeechSynthesizer); " +
                "$speechSynthesizer.GetInstalledVoices((New-Object System.Globalization.CultureInfo('" + culture + "'))).VoiceInfo.Name;";
    }

    public void setup() throws IOException {
        voices = new HashMap<>();
        List<String> cultures = powerShellResult(getCulturesCommand());
        for (String culture : cultures) {
            voices.put(culture.replace('-', '_'), powerShellResult(getVoicesCommand(culture)));
        }
    }

    private List<String> powerShellResult(String powerShellCommand) throws IOException {
        LOGGER.fine(powerShellCommand);
        String command = "powershell.exe \"" + powerShellCommand + "\"";
        powerShellProcess = Runtime.getRuntime().exec(command);
        powerShellProcess.getOutputStream().close();

        String line;
        List<String> result = new ArrayList<>();
        try (BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()))) {
            while ((line = stdout.readLine()) != null) {
                result.add(line);
            }
        }
        try (BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()))) {
            while ((line = stderr.readLine()) != null) {
                LOGGER.severe(line);
            }
        }
        powerShellProcess.destroy();
        return result;
    }

    @Override
    public void play(String language, String text) {
        String selectedVoice = selectVoice(language);
        String command = "Add-Type -AssemblyName System.Speech; " +
                "$speechSynthesizer=(New-Object System.Speech.Synthesis.SpeechSynthesizer); " +
                (selectedVoice != null ? "$speechSynthesizer.SelectVoice('" + selectedVoice + "'); " : "") +
                "$speechSynthesizer.rate=0; " +
                "$speechSynthesizer.Speak('" + text.replace("'", "''") + "');";
        try {
            LOGGER.info(language + " " + text);
            powerShellResult(command);
        } catch (IOException e) {
            LOGGER.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    private String selectVoice(String language) {
        if (voices == null || voices.isEmpty() || language == null || language.isBlank()) {
            return null;
        }
        String normalizedLanguage = language.replace('-', '_');
        List<String> exactVoices = voices.get(normalizedLanguage);
        if (exactVoices != null && !exactVoices.isEmpty()) {
            return exactVoices.get(0);
        }

        String languagePrefix = normalizedLanguage.contains("_")
                ? normalizedLanguage.substring(0, normalizedLanguage.indexOf('_'))
                : normalizedLanguage;
        for (Map.Entry<String, List<String>> entry : voices.entrySet()) {
            if (entry.getKey().startsWith(languagePrefix + "_") && !entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return null;
    }

    @Override
    public void abort() {
        if (powerShellProcess != null && powerShellProcess.isAlive()) {
            powerShellProcess.destroy();
        }
    }
}