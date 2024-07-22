package org.leo.dictionary.audio;

import java.util.logging.Logger;

public class TestAudioService implements AudioService {
    private final static Logger LOGGER = Logger.getLogger(TestAudioService.class.getName());

    @Override
    public void play(String language, String text) {
        LOGGER.info(language + " " + text);
    }

    @Override
    public void abort() {
        LOGGER.info("aborted");
    }
}
