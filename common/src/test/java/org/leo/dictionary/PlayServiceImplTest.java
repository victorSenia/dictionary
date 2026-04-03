package org.leo.dictionary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.leo.dictionary.audio.AudioService;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.FileConfigurationReader;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.WordProvider;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void playWordCallsAudioWithExpectedWordAndUpdatesKnowledge() throws Exception {
        Path configPath = tempDir.resolve("play.properties");
        writePlayProperties(configPath, true, false, false);

        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath(configPath.toString());
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(fileConfigurationReader);

        List<String> audioCalls = new ArrayList<>();
        AudioService audioService = new AudioService() {
            @Override
            public void play(String language, String text) {
                audioCalls.add(language + "|" + text);
            }

            @Override
            public void abort() {
            }
        };

        AtomicReference<Word> updatedWordRef = new AtomicReference<>();
        WordProvider wordProvider = new WordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, org.leo.dictionary.entity.Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, Set<org.leo.dictionary.entity.Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }

            @Override
            public List<Word> findKnownWords() {
                return List.of();
            }

            @Override
            public void updateWord(Word word) {
                updatedWordRef.set(word);
            }
        };

        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordUpdater(wordProvider);

        Word word = new Word();
        word.setLanguage("de");
        word.setArticle("der ");
        word.setWord("Hund");
        Translation translation = new Translation();
        translation.setLanguage("en");
        translation.setTranslation("dog");
        word.setTranslations(List.of(translation));
        double initialKnowledge = word.getKnowledge();
        playService.setWords(List.of(word));

        invokePlayWord(playService, word);

        assertEquals(List.of("de|der Hund"), audioCalls);
        assertNotNull(updatedWordRef.get());
        assertTrue(word.getKnowledge() > initialKnowledge);
        assertEquals(0.5, word.getKnowledge(), 0.0001);
    }

    @Test
    void playWordUsesTranslationFilterAndExpectedLanguages() throws Exception {
        Path configPath = tempDir.resolve("play-translation.properties");
        writePlayProperties(configPath, false, true, true);

        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath(configPath.toString());
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(fileConfigurationReader);

        List<String> audioCalls = new ArrayList<>();
        AudioService audioService = new AudioService() {
            @Override
            public void play(String language, String text) {
                audioCalls.add(language + "|" + text);
            }

            @Override
            public void abort() {
            }
        };

        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordUpdater(new WordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, org.leo.dictionary.entity.Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, Set<org.leo.dictionary.entity.Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }

            @Override
            public List<Word> findKnownWords() {
                return List.of();
            }
        });

        Word word = new Word();
        word.setLanguage("de");
        word.setWord("Haus");
        Translation en = new Translation();
        en.setLanguage("en");
        en.setTranslation("house");
        Translation es = new Translation();
        es.setLanguage("es");
        es.setTranslation("casa");
        word.setTranslations(List.of(en, es));

        playService.setWords(List.of(word));
        playService.setPlayTranslationFor(Set.of("en"));
        invokePlayWord(playService, word);

        assertEquals(List.of("de|Haus", "en|house"), audioCalls);
        String joined = audioCalls.stream().collect(Collectors.joining(","));
        assertFalse(joined.contains("es|casa"));
    }

    private static void writePlayProperties(Path path, boolean includeArticle, boolean translationActive, boolean allTranslations) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("org.leo.dictionary.config.entity.General.knowledgeIncrease", "0.5");
        properties.setProperty("org.leo.dictionary.config.entity.General.delayBefore", "0");
        properties.setProperty("org.leo.dictionary.config.entity.General.delayPerLetterAfter", "0");
        properties.setProperty("org.leo.dictionary.config.entity.General.includeArticle", Boolean.toString(includeArticle));
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.times", "1");
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.active", Boolean.toString(translationActive));
        properties.setProperty("org.leo.dictionary.config.entity.Translation.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.eachTime", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.allTranslations", Boolean.toString(allTranslations));
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.active", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.eachTime", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.letterDelay", "0");
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, "test");
        }
    }

    private static void invokePlayWord(PlayServiceImpl playService, Word word) throws Exception {
        Method method = PlayServiceImpl.class.getDeclaredMethod("playWord", Word.class);
        method.setAccessible(true);
        method.invoke(playService, word);
    }

    @Test
    void playFromNextAndPreviousStartFromExpectedWords() throws Exception {
        PlayServiceImpl playFromService = createPlayServiceWithSimpleConfig(tempDir.resolve("play-from.properties"), List.of());
        Word one = createWord("eins");
        Word two = createWord("zwei");
        playFromService.setWords(List.of(one, two));

        AtomicReference<String> firstFromSecond = captureFirstPlayedText(playFromService);
        playFromService.playFrom(1);
        waitForCondition(() -> firstFromSecond.get() != null, 1000);
        playFromService.pause();
        waitForCondition(() -> !playFromService.isPlaying(), 1000);
        assertEquals("de|zwei", firstFromSecond.get());

        PlayServiceImpl playFromNegativeIndexService = createPlayServiceWithSimpleConfig(tempDir.resolve("play-from-negative.properties"), List.of());
        playFromNegativeIndexService.setWords(List.of(createWord("eins"), createWord("zwei")));
        AtomicReference<String> firstFromNegative = captureFirstPlayedText(playFromNegativeIndexService);
        playFromNegativeIndexService.playFrom(-1);
        waitForCondition(() -> firstFromNegative.get() != null, 1000);
        playFromNegativeIndexService.pause();
        waitForCondition(() -> !playFromNegativeIndexService.isPlaying(), 1000);
        assertEquals("de|zwei", firstFromNegative.get());

        PlayServiceImpl nextService = createPlayServiceWithSimpleConfig(tempDir.resolve("next.properties"), List.of());
        nextService.setWords(List.of(createWord("eins"), createWord("zwei")));
        AtomicReference<String> firstAfterNext = captureFirstPlayedText(nextService);
        nextService.next();
        waitForCondition(() -> firstAfterNext.get() != null, 1000);
        nextService.pause();
        waitForCondition(() -> !nextService.isPlaying(), 1000);
        assertEquals("de|zwei", firstAfterNext.get());

        PlayServiceImpl previousService = createPlayServiceWithSimpleConfig(tempDir.resolve("previous.properties"), List.of());
        previousService.setWords(List.of(createWord("eins"), createWord("zwei")));
        AtomicReference<String> firstAfterPrevious = captureFirstPlayedText(previousService);
        previousService.previous();
        waitForCondition(() -> firstAfterPrevious.get() != null, 1000);
        previousService.pause();
        waitForCondition(() -> !previousService.isPlaying(), 1000);
        assertEquals("de|zwei", firstAfterPrevious.get());
    }

    @Test
    void pauseStopsPlaybackAndCallsAbort() throws Exception {
        ConfigurationService configurationService = new ConfigurationService() {
            private final org.leo.dictionary.config.entity.Configuration configuration = createInMemoryConfiguration();

            @Override
            public org.leo.dictionary.config.entity.Configuration getConfiguration() {
                return configuration;
            }
        };

        AtomicInteger abortCalls = new AtomicInteger();
        AudioService audioService = new AudioService() {
            @Override
            public void play(String language, String text) {
            }

            @Override
            public void abort() {
                abortCalls.incrementAndGet();
            }
        };

        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordUpdater(new WordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, org.leo.dictionary.entity.Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, Set<org.leo.dictionary.entity.Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }

            @Override
            public List<Word> findKnownWords() {
                return List.of();
            }
        });
        playService.setWords(List.of(createWord("eins")));

        playService.play();
        waitForCondition(playService::isPlaying, 1000);

        playService.pause();
        waitForCondition(() -> !playService.isPlaying(), 1000);

        assertTrue(abortCalls.get() >= 1);
    }

    private static org.leo.dictionary.config.entity.Configuration createInMemoryConfiguration() {
        org.leo.dictionary.config.entity.Configuration configuration = new org.leo.dictionary.config.entity.Configuration();
        java.util.Map<Object, Object> properties = new java.util.HashMap<>();
        properties.put("org.leo.dictionary.config.entity.General.knowledgeIncrease", 0.5d);
        properties.put("org.leo.dictionary.config.entity.General.delayBefore", 5_000L);
        properties.put("org.leo.dictionary.config.entity.General.delayPerLetterAfter", 0L);
        properties.put("org.leo.dictionary.config.entity.General.includeArticle", false);
        properties.put("org.leo.dictionary.config.entity.Repeat.times", 1);
        properties.put("org.leo.dictionary.config.entity.Repeat.delay", 0L);
        properties.put("org.leo.dictionary.config.entity.Translation.active", false);
        properties.put("org.leo.dictionary.config.entity.Spelling.active", false);
        configuration.setProperties(properties);
        return configuration;
    }

    @Test
    void playWordCyclesTranslationsAcrossRepeatsWhenEachTimeEnabled() throws Exception {
        Path configPath = tempDir.resolve("play-repeat-translation.properties");
        writePlayProperties(configPath, false, true, false, true, 3);

        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath(configPath.toString());
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(fileConfigurationReader);

        List<String> audioCalls = new ArrayList<>();
        AudioService audioService = new AudioService() {
            @Override
            public void play(String language, String text) {
                audioCalls.add(language + "|" + text);
            }

            @Override
            public void abort() {
            }
        };

        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordUpdater(new WordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, org.leo.dictionary.entity.Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, Set<org.leo.dictionary.entity.Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }

            @Override
            public List<Word> findKnownWords() {
                return List.of();
            }
        });

        Word word = new Word();
        word.setLanguage("de");
        word.setWord("Haus");
        Translation one = new Translation();
        one.setLanguage("en");
        one.setTranslation("house");
        Translation two = new Translation();
        two.setLanguage("en");
        two.setTranslation("home");
        word.setTranslations(List.of(one, two));

        playService.setWords(List.of(word));
        invokePlayWord(playService, word);

        assertEquals(List.of("de|Haus", "en|house", "de|Haus", "en|home", "de|Haus", "en|house"), audioCalls);
    }

    @Test
    void playWordSpellsLettersWhenSpellingIsActive() throws Exception {
        Path configPath = tempDir.resolve("play-spelling.properties");
        writePlayPropertiesWithSpelling(configPath, true, 0, 0);

        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath(configPath.toString());
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(fileConfigurationReader);

        List<String> audioCalls = new ArrayList<>();
        AudioService audioService = new AudioService() {
            @Override
            public void play(String language, String text) {
                audioCalls.add(language + "|" + text);
            }

            @Override
            public void abort() {
            }
        };

        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordUpdater(new WordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, org.leo.dictionary.entity.Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, Set<org.leo.dictionary.entity.Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }

            @Override
            public List<Word> findKnownWords() {
                return List.of();
            }
        });

        Word word = new Word();
        word.setLanguage("de");
        word.setWord("ab");
        word.setTranslations(List.of());

        playService.setWords(List.of(word));
        invokePlayWord(playService, word);

        assertEquals(List.of("de|ab", "de|A", "de|B"), audioCalls);
    }

    private static PlayServiceImpl createPlayServiceWithSimpleConfig(Path configPath, List<String> audioCalls) throws IOException {
        return createPlayServiceWithSimpleConfig(configPath, audioCalls, new AtomicInteger());
    }

    private static PlayServiceImpl createPlayServiceWithSimpleConfig(Path configPath, List<String> audioCalls, AtomicInteger abortCalls) throws IOException {
        writePlayProperties(configPath, false, false, false);

        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath(configPath.toString());
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(fileConfigurationReader);

        AudioService audioService = new AudioService() {
            @Override
            public void play(String language, String text) {
                if (audioCalls != null) {
                    audioCalls.add(language + "|" + text);
                }
            }

            @Override
            public void abort() {
                abortCalls.incrementAndGet();
            }
        };

        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordUpdater(new WordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, org.leo.dictionary.entity.Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopicsWithRoot(String language, Set<org.leo.dictionary.entity.Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<org.leo.dictionary.entity.Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }

            @Override
            public List<Word> findKnownWords() {
                return List.of();
            }
        });
        return playService;
    }

    private static void writePlayProperties(Path path, boolean includeArticle, boolean translationActive, boolean allTranslations,
                                            boolean translationEachTime, int repeatTimes) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("org.leo.dictionary.config.entity.General.knowledgeIncrease", "0.5");
        properties.setProperty("org.leo.dictionary.config.entity.General.delayBefore", "0");
        properties.setProperty("org.leo.dictionary.config.entity.General.delayPerLetterAfter", "0");
        properties.setProperty("org.leo.dictionary.config.entity.General.includeArticle", Boolean.toString(includeArticle));
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.times", Integer.toString(repeatTimes));
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.active", Boolean.toString(translationActive));
        properties.setProperty("org.leo.dictionary.config.entity.Translation.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.eachTime", Boolean.toString(translationEachTime));
        properties.setProperty("org.leo.dictionary.config.entity.Translation.allTranslations", Boolean.toString(allTranslations));
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.active", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.eachTime", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.letterDelay", "0");
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, "test");
        }
    }

    private static void writePlayPropertiesWithSpelling(Path path, boolean spellingActive, long spellingDelay, long letterDelay) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("org.leo.dictionary.config.entity.General.knowledgeIncrease", "0.5");
        properties.setProperty("org.leo.dictionary.config.entity.General.delayBefore", "0");
        properties.setProperty("org.leo.dictionary.config.entity.General.delayPerLetterAfter", "0");
        properties.setProperty("org.leo.dictionary.config.entity.General.includeArticle", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.times", "1");
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.active", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.delay", "0");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.eachTime", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Translation.allTranslations", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.active", Boolean.toString(spellingActive));
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.eachTime", "false");
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.delay", String.valueOf(spellingDelay));
        properties.setProperty("org.leo.dictionary.config.entity.Spelling.letterDelay", String.valueOf(letterDelay));
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, "test");
        }
    }

    private static Word createWord(String value) {
        Word word = new Word();
        word.setLanguage("de");
        word.setWord(value);
        word.setTranslations(List.of());
        return word;
    }

    private static AtomicReference<String> captureFirstPlayedText(PlayServiceImpl playService) {
        AtomicReference<String> firstPlayed = new AtomicReference<>();
        AudioService original = new AudioService() {
            @Override
            public void play(String language, String text) {
                firstPlayed.compareAndSet(null, language + "|" + text);
            }

            @Override
            public void abort() {
            }
        };
        playService.setAudioService(original);
        return firstPlayed;
    }

    private static void waitForCondition(Condition condition, long timeoutMs) throws InterruptedException {
        long stopAt = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < stopAt) {
            if (condition.matches()) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Condition was not met in " + timeoutMs + " ms");
    }

    @FunctionalInterface
    private interface Condition {
        boolean matches();
    }
}
