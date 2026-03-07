package org.leo.dictionary.word.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.leo.dictionary.config.entity.ParseWords;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileWordProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void importerExporterEncodingRoundTripsSpecialCharacters() {
        String raw = "de:cat;dog=fast value";
        String encoded = WordExporter.encode(raw);
        String decoded = WordImporter.decode(encoded);
        assertEquals(raw, decoded);
    }

    @Test
    void parseAndUpdateConfigurationReadsLineFromFile() throws IOException {
        Path path = tempDir.resolve("parse-config.txt");
        ParseWords expected = createBaseConfiguration(path);
        expected.setRootTopic("A1 Root");
        expected.setTranslationDelimiter(",");
        expected.setAdditionalInformationDelimiter("#");

        FileWordProvider writerProvider = new FileWordProvider();
        writerProvider.setConfiguration(expected);
        String configurationLine = writerProvider.configurationToLine();
        writeLines(path, List.of(configurationLine, "die Katze#noun|cat|gato"));

        ParseWords loaded = createBaseConfiguration(path);
        loaded.setRootTopic("");
        loaded.setTranslationDelimiter(";");
        loaded.setAdditionalInformationDelimiter(";");
        FileWordProvider readerProvider = new FileWordProvider();
        readerProvider.setConfiguration(loaded);

        readerProvider.parseAndUpdateConfiguration();

        assertEquals("de", loaded.getLanguageFrom());
        assertEquals(List.of("en", "es"), loaded.getLanguagesTo());
        assertEquals(List.of("der ", "die ", "das "), loaded.getArticles());
        assertEquals("\\|", loaded.getDelimiter());
        assertEquals("#", loaded.getAdditionalInformationDelimiter());
        assertEquals(",", loaded.getTranslationDelimiter());
        assertEquals("\t", loaded.getTopicFlag());
        assertEquals("", loaded.getTopicDelimiter());
        assertEquals("A1 Root", loaded.getRootTopic());
    }

    @Test
    void loadWordsMergesDuplicatesAndSupportsFiltering() throws IOException {
        Path path = tempDir.resolve("words.txt");
        ParseWords configuration = createBaseConfiguration(path);
        configuration.setRootTopic("Main");
        configuration.setTranslationDelimiter(",");

        FileWordProvider provider = new FileWordProvider();
        provider.setConfiguration(configuration);
        String configLine = provider.configurationToLine();
        writeLines(path, List.of(
                configLine,
                "\tAnimals",
                "der Hund;noun|dog,hound|perro",
                "der Hund;noun|canine|doggo",
                "\tPlants",
                "die Rose|rose|rosa"
        ));

        List<Word> allWords = provider.findWords(new WordCriteria());
        assertEquals(2, allWords.size());

        Word hund = allWords.stream().filter(w -> "Hund".equals(w.getWord())).findFirst().orElse(null);
        assertNotNull(hund);
        assertEquals("der ", hund.getArticle());
        assertEquals("noun", hund.getAdditionalInformation());
        Set<String> translations = hund.getTranslations().stream().map(Translation::getTranslation).collect(Collectors.toSet());
        assertTrue(translations.containsAll(Set.of("dog", "hound", "perro", "canine", "doggo")));
        assertEquals(5, translations.size());
        assertNotNull(hund.getTopics());
        assertTrue(hund.getTopics().stream().map(Topic::getName).collect(Collectors.toSet()).contains("Animals"));

        Word rose = allWords.stream().filter(w -> "Rose".equals(w.getWord())).findFirst().orElse(null);
        assertNotNull(rose);
        rose.setKnowledge(0.2);
        hund.setKnowledge(0.9);

        WordCriteria topicFilter = new WordCriteria();
        Topic animals = new Topic();
        animals.setName("Animals");
        topicFilter.setTopicsOr(Set.of(animals));
        List<Word> onlyAnimals = provider.findWords(topicFilter);
        assertEquals(1, onlyAnimals.size());
        assertEquals("Hund", onlyAnimals.get(0).getWord());

        WordCriteria knowledgeFilter = new WordCriteria();
        knowledgeFilter.setKnowledgeFrom(0.5);
        List<Word> knownWords = provider.findWords(knowledgeFilter);
        assertEquals(1, knownWords.size());
        assertEquals("Hund", knownWords.get(0).getWord());
    }

    @Test
    void findWordsSupportsKnowledgeToSortedAndDeterministicShuffle() throws IOException {
        Path path = tempDir.resolve("order-and-limits.txt");
        ParseWords configuration = createBaseConfiguration(path);
        configuration.setRootTopic("Main");
        configuration.setTranslationDelimiter(",");

        FileWordProvider provider = new FileWordProvider();
        provider.setConfiguration(configuration);
        String configLine = provider.configurationToLine();
        writeLines(path, List.of(
                configLine,
                "\tAnimals",
                "die Zitrone|lemon|limon",
                "das Auto|car|coche",
                "der Baum|tree|arbol"
        ));

        List<Word> imported = provider.findWords(new WordCriteria());
        Word zitrone = imported.stream().filter(w -> "Zitrone".equals(w.getWord())).findFirst().orElseThrow();
        Word auto = imported.stream().filter(w -> "Auto".equals(w.getWord())).findFirst().orElseThrow();
        Word baum = imported.stream().filter(w -> "Baum".equals(w.getWord())).findFirst().orElseThrow();
        zitrone.setKnowledge(0.1);
        auto.setKnowledge(0.4);
        baum.setKnowledge(0.8);

        WordCriteria knowledgeTo = new WordCriteria();
        knowledgeTo.setKnowledgeTo(0.5);
        List<Word> belowLimit = provider.findWords(knowledgeTo);
        assertEquals(2, belowLimit.size());
        assertTrue(belowLimit.stream().map(Word::getWord).collect(Collectors.toSet()).containsAll(Set.of("Zitrone", "Auto")));

        WordCriteria sorted = new WordCriteria();
        sorted.setWordsOrderMode(WordCriteria.WordsOrderMode.SORTED);
        List<Word> sortedWords = provider.findWords(sorted);
        assertEquals(List.of("Auto", "Baum", "Zitrone"), sortedWords.stream().map(Word::getWord).collect(Collectors.toList()));

        WordCriteria shuffleOne = new WordCriteria();
        shuffleOne.setWordsOrderMode(WordCriteria.WordsOrderMode.SHUFFLE);
        shuffleOne.setShuffleRandom(123L);
        List<String> shuffledOne = provider.findWords(shuffleOne).stream().map(Word::getWord).collect(Collectors.toList());

        WordCriteria shuffleTwo = new WordCriteria();
        shuffleTwo.setWordsOrderMode(WordCriteria.WordsOrderMode.SHUFFLE);
        shuffleTwo.setShuffleRandom(123L);
        List<String> shuffledTwo = provider.findWords(shuffleTwo).stream().map(Word::getWord).collect(Collectors.toList());
        assertEquals(shuffledOne, shuffledTwo);
    }

    @Test
    void findWordsFallsBackToShuffleWhenModeIsNullAndSeedIsProvided() throws IOException {
        Path path = tempDir.resolve("shuffle-fallback.txt");
        ParseWords configuration = createBaseConfiguration(path);
        configuration.setRootTopic("Main");
        configuration.setTranslationDelimiter(",");

        FileWordProvider provider = new FileWordProvider();
        provider.setConfiguration(configuration);
        String configLine = provider.configurationToLine();
        writeLines(path, List.of(
                configLine,
                "\tAnimals",
                "die Zitrone|lemon|limon",
                "das Auto|car|coche",
                "der Baum|tree|arbol"
        ));

        WordCriteria fallbackShuffleOne = new WordCriteria();
        fallbackShuffleOne.setWordsOrderMode(null);
        fallbackShuffleOne.setShuffleRandom(42L);
        List<String> shuffledOne = provider.findWords(fallbackShuffleOne).stream().map(Word::getWord).collect(Collectors.toList());

        WordCriteria fallbackShuffleTwo = new WordCriteria();
        fallbackShuffleTwo.setWordsOrderMode(null);
        fallbackShuffleTwo.setShuffleRandom(42L);
        List<String> shuffledTwo = provider.findWords(fallbackShuffleTwo).stream().map(Word::getWord).collect(Collectors.toList());

        assertEquals(shuffledOne, shuffledTwo);
        assertEquals(3, shuffledOne.size());
    }

    @Test
    void findTopicsRespectsRequestedLevel() throws IOException {
        Path path = tempDir.resolve("topics-levels.txt");
        ParseWords configuration = createBaseConfiguration(path);
        configuration.setRootTopic("Main");
        configuration.setTranslationDelimiter(",");

        FileWordProvider provider = new FileWordProvider();
        provider.setConfiguration(configuration);
        String configLine = provider.configurationToLine();
        writeLines(path, List.of(
                configLine,
                "\tAnimals",
                "\t\tPets",
                "der Hund|dog|perro"
        ));

        List<Topic> levelTwo = provider.findTopics("de", 2);
        assertEquals(1, levelTwo.size());
        assertEquals("Animals", levelTwo.get(0).getName());

        List<Topic> allLevels = provider.findTopics("de", 3);
        assertTrue(allLevels.stream().map(Topic::getName).collect(Collectors.toSet()).containsAll(Set.of("Animals", "Pets")));
    }

    @Test
    void configurationLineRoundTripsSpecialCharacters() throws IOException {
        Path path = tempDir.resolve("config-special.txt");
        ParseWords configuration = createBaseConfiguration(path);
        configuration.setLanguageFrom("de-DE");
        configuration.setLanguagesTo(List.of("en-US", "es-ES"));
        configuration.setArticles(List.of("der ", "d:ie ", "da;s "));
        configuration.setDelimiter("\\|");
        configuration.setAdditionalInformationDelimiter(":");
        configuration.setTranslationDelimiter(";");
        configuration.setTopicFlag("\t");
        configuration.setTopicDelimiter(".");
        configuration.setRootTopic("A1:Basics;Core");

        FileWordProvider writerProvider = new FileWordProvider();
        writerProvider.setConfiguration(configuration);
        String configLine = writerProvider.configurationToLine();
        writeLines(path, List.of(configLine));

        ParseWords loaded = createBaseConfiguration(path);
        FileWordProvider readerProvider = new FileWordProvider();
        readerProvider.setConfiguration(loaded);
        readerProvider.parseAndUpdateConfiguration();

        assertEquals("de-DE", loaded.getLanguageFrom());
        assertEquals(List.of("en-US", "es-ES"), loaded.getLanguagesTo());
        assertEquals(List.of("der ", "d:ie ", "da;s "), loaded.getArticles());
        assertEquals("\\|", loaded.getDelimiter());
        assertEquals(":", loaded.getAdditionalInformationDelimiter());
        assertEquals(";", loaded.getTranslationDelimiter());
        assertEquals("\t", loaded.getTopicFlag());
        assertEquals(".", loaded.getTopicDelimiter());
        assertEquals("A1:Basics;Core", loaded.getRootTopic());
    }

    @Test
    void parseAndUpdateConfigurationThrowsForMalformedConfigurationLine() throws IOException {
        Path path = tempDir.resolve("config-malformed.txt");
        writeLines(path, List.of(
                FileWordProvider.PARSE_WORDS_CONFIGURATION + ":de:en",
                "der Hund|dog"
        ));
        ParseWords configuration = createBaseConfiguration(path);

        FileWordProvider provider = new FileWordProvider();
        provider.setConfiguration(configuration);

        assertThrows(IllegalArgumentException.class, provider::parseAndUpdateConfiguration);
    }

    @Test
    void listPropertyHelpersHandleNullEmptyAndRoundTrip() {
        assertEquals("", FileWordProvider.listPropertyToString(null));
        assertEquals("", FileWordProvider.listPropertyToString(List.of()));
        assertEquals(List.of(), FileWordProvider.parseListProperty("  "));
        assertEquals("", FileWordProvider.decode(""));
        assertEquals("", FileWordProvider.decode(null));

        List<String> values = List.of("de", "en:us", "a;b");
        String encoded = FileWordProvider.listPropertyToString(values);
        assertEquals(values, FileWordProvider.parseListProperty(encoded));
    }

    private static ParseWords createBaseConfiguration(Path path) {
        ParseWords configuration = new ParseWords();
        configuration.setProperties(new HashMap<>());
        configuration.setPath(path.toString());
        configuration.setLanguageFrom("de");
        configuration.setLanguagesTo(List.of("en", "es"));
        configuration.setArticles(List.of("der ", "die ", "das "));
        configuration.setDelimiter("\\|");
        configuration.setAdditionalInformationDelimiter(";");
        configuration.setTranslationDelimiter(";");
        configuration.setTopicFlag("\t");
        configuration.setTopicDelimiter("");
        configuration.setRootTopic("");
        return configuration;
    }

    private static void writeLines(Path path, List<String> lines) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
    }
}
