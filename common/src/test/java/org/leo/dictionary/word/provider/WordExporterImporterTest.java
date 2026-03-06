package org.leo.dictionary.word.provider;

import org.junit.jupiter.api.Test;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WordExporterImporterTest {

    @Test
    void writeThenReadRoundTripWithTopicAndTranslations() throws IOException {
        Word word = new Word();
        word.setLanguage("de");
        word.setArticle("der ");
        word.setWord("Hund");
        word.setAdditionalInformation("noun info");

        Translation en = new Translation();
        en.setLanguage("en");
        en.setTranslation("house dog");
        Translation es = new Translation();
        es.setLanguage("es");
        es.setTranslation("perro");
        word.setTranslations(List.of(en, es));

        Topic root = new Topic();
        root.setLanguage("de");
        root.setLevel(1);
        root.setName("A1");
        Topic topic = new Topic();
        topic.setLanguage("de");
        topic.setLevel(2);
        topic.setName("Animals");
        topic.setRoot(root);
        word.setTopics(List.of(topic));

        StringWriter sink = new StringWriter();
        WordExporter exporter = new WordExporter() {
            @Override
            protected BufferedWriter getBufferedWriter() {
                return new BufferedWriter(sink);
            }
        };
        exporter.writeWords(List.of(word), false, List.of("A1"));

        WordImporter importer = new WordImporter() {
            @Override
            protected BufferedReader getBufferedReader() {
                return new BufferedReader(new StringReader(sink.toString()));
            }
        };

        List<Word> imported = importer.readWords();
        assertEquals(1, imported.size());
        Word importedWord = imported.get(0);
        assertEquals("de", importedWord.getLanguage());
        assertEquals("der ", importedWord.getArticle());
        assertEquals("Hund", importedWord.getWord());
        assertEquals("noun info", importedWord.getAdditionalInformation());
        assertEquals(2, importedWord.getTranslations().size());
        assertEquals("en", importedWord.getTranslations().get(0).getLanguage());
        assertEquals("house dog", importedWord.getTranslations().get(0).getTranslation());
        assertEquals("es", importedWord.getTranslations().get(1).getLanguage());
        assertEquals("perro", importedWord.getTranslations().get(1).getTranslation());
        assertEquals(1, importedWord.getTopics().size());
        Topic importedTopic = importedWord.getTopics().get(0);
        assertEquals(2, importedTopic.getLevel());
        assertEquals("Animals", importedTopic.getName());
        assertNotNull(importedTopic.getRoot());
        assertEquals("A1", importedTopic.getRoot().getName());
    }

    @Test
    void importerSkipsMalformedLinesAndReadsValidOnes() throws IOException {
        String text = String.join(System.lineSeparator(),
                "CONFIGURATION_PREFIX:de",
                "invalid-line-without-required-parts",
                "der+:Hund+:info+:en=dog;+:",
                ""
        );

        WordImporter importer = new WordImporter() {
            @Override
            protected BufferedReader getBufferedReader() {
                return new BufferedReader(new StringReader(text));
            }
        };

        List<Word> words = importer.readWords();
        assertEquals(1, words.size());
        Word word = words.get(0);
        assertEquals("de", word.getLanguage());
        assertEquals("der ", word.getArticle());
        assertEquals("Hund ", word.getWord());
        assertEquals("info ", word.getAdditionalInformation());
        assertEquals(1, word.getTranslations().size());
        assertEquals("en", word.getTranslations().get(0).getLanguage());
        assertEquals("dog", word.getTranslations().get(0).getTranslation());
        assertEquals(0, word.getTopics().size());
    }

    @Test
    void writeWordsAllForLanguageWritesSectionPerRequestedRoot() throws IOException {
        Word noTopic = createWord("de", "das ", "Wasser", "noun", List.of(translation("en", "water")), null);
        Topic a1Root = topic("de", 1, "A1", null);
        Topic b1Root = topic("de", 1, "B1", null);
        Word a1Word = createWord("de", "der ", "Hund", "noun", List.of(translation("en", "dog")), List.of(topic("de", 2, "Animals", a1Root)));
        Word b1Word = createWord("de", "die ", "Rose", "noun", List.of(translation("en", "rose")), List.of(topic("de", 2, "Plants", b1Root)));

        StringWriter sink = new StringWriter();
        WordExporter exporter = new WordExporter() {
            @Override
            protected BufferedWriter getBufferedWriter() {
                return new BufferedWriter(sink);
            }
        };
        exporter.writeWords(List.of(noTopic, a1Word, b1Word), true, List.of("A1", "B1"));

        List<String> lines = Arrays.stream(sink.toString().split("\\R"))
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
        long configurationLines = lines.stream().filter(line -> line.startsWith(WordExporter.CONFIGURATION_PREFIX)).count();
        assertEquals(3, configurationLines);

        WordImporter importer = new WordImporter() {
            @Override
            protected BufferedReader getBufferedReader() {
                return new BufferedReader(new StringReader(sink.toString()));
            }
        };
        List<Word> imported = importer.readWords();
        assertEquals(3, imported.size());
        assertEquals(List.of("Wasser", "Hund", "Rose"), imported.stream().map(Word::getWord).collect(Collectors.toList()));
    }

    private static Word createWord(String language, String article, String value, String info, List<Translation> translations, List<Topic> topics) {
        Word word = new Word();
        word.setLanguage(language);
        word.setArticle(article);
        word.setWord(value);
        word.setAdditionalInformation(info);
        word.setTranslations(translations);
        word.setTopics(topics);
        return word;
    }

    private static Translation translation(String language, String value) {
        Translation translation = new Translation();
        translation.setLanguage(language);
        translation.setTranslation(value);
        return translation;
    }

    private static Topic topic(String language, int level, String name, Topic root) {
        Topic topic = new Topic();
        topic.setLanguage(language);
        topic.setLevel(level);
        topic.setName(name);
        topic.setRoot(root);
        return topic;
    }
}
