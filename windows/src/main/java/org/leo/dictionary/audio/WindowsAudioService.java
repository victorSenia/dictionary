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
    private final static Logger LOGGER = Logger.getLogger(WindowsAudioService.class.getName());
    private Map<String, List<String>> voices;

    private Process powerShellProcess = null;

    public void setup() throws IOException {
        voices = new HashMap<>();
        List<String> cultures = powerShellResult(getCulturesCommand());
        for (String culture : cultures) {
            voices.put(culture.replace('-', '_'), powerShellResult(getVoicesCommand(culture)));
        }
    }

    private static String getCulturesCommand() {
        return "Add-Type -AssemblyName System.Speech; $speechSynthesizer=(New-Object System.Speech.Synthesis.SpeechSynthesizer); " +
                "$speechSynthesizer.GetInstalledVoices().VoiceInfo.Culture.Name;";
    }

    private static String getVoicesCommand(String Culture) {
        return "Add-Type -AssemblyName System.Speech; $speechSynthesizer=(New-Object System.Speech.Synthesis.SpeechSynthesizer); " +
                "$speechSynthesizer.GetInstalledVoices((New-Object CultureInfo('" + Culture + "'))).VoiceInfo.Name;";
    }

    private List<String> powerShellResult(String powerShellCommand) throws IOException {
        LOGGER.fine(powerShellCommand);
        String command = "powershell.exe \"" + powerShellCommand + "\"";
// Executing the command
        powerShellProcess = Runtime.getRuntime().exec(command);
// Getting the results
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
        String command = "Add-Type –AssemblyName System.Speech; " +
                "$speechSynthesizer=(New-Object System.Speech.Synthesis.SpeechSynthesizer); " +
                (voices.containsKey(language) ? "$speechSynthesizer.SelectVoice('" + voices.get(language).get(0) + "'); " : "") +
                "$speechSynthesizer.rate=0; " +
                "$speechSynthesizer.Speak('" + text.replace("'","''") + "');";
        try {//TODO language setting
            LOGGER.info(language + " " + text);
            powerShellResult(command);
        } catch (IOException e) {
            LOGGER.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void abort() {
        if (powerShellProcess != null && powerShellProcess.isAlive()) {
            powerShellProcess.destroy();
        }
    }
}
