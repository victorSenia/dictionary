package org.leo.dictionary.word.provider;

import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * format of word from {@link  WordExporter}
 */
public abstract class WordImporter {
    public static final int ARTICLE_INDEX = 0;
    public static final int WORD_INDEX = 1;
    public static final int ADDITIONAL_INFORMATION_INDEX = 2;
    public static final int TRANSLATIONS_INDEX = 3;
    public static final int TOPICS_INDEX = 4;
    private final static Logger LOGGER = Logger.getLogger(WordImporter.class.getName());
    private static final int WORD_PARTS = 5;
    private static final int TRANSLATION_PARTS = 2;
    private static final int TRANSLATION_LANGUAGE_INDEX = 0;
    private static final int TRANSLATION_TRANSLATION_INDEX = 1;
    private static final int TOPIC_PARTS = 2;
    private static final int TOPIC_LEVEL_INDEX = 0;
    private static final int TOPIC_NAME_WORD_INDEX = 1;
    private String language;
    private Topic rootTopic;

    private static boolean notEmptyString(String string) {
        return string != null && !string.isEmpty();
    }

    public static String decode(String string) {
        if (notEmptyString(string)) {
            return URLDecoder.decode(string, StandardCharsets.UTF_8);
        }
        return "";
    }

    protected abstract BufferedReader getBufferedReader();

    public List<Word> readWords() throws IOException {
        List<Word> words = new ArrayList<>();
        try (BufferedReader reader = getBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(WordExporter.CONFIGURATION_PREFIX)) {
                    String[] parts = line.split(WordExporter.MAIN_DIVIDER);
                    language = parts[1];
                    rootTopic = createTopicIfNeeded(parts);
                }
                String[] parts = line.split(WordExporter.MAIN_DIVIDER, -1);
                if (parts.length != WORD_PARTS) {
                    LOGGER.info("Wrong word format " + line);
                    continue;
                }
                Word word = parseWord(parts);
                words.add(word);
            }
            return words;
        }
    }

    private Topic createTopicIfNeeded(String[] parts) {
        if (parts.length > 2) {
            Topic topic = new Topic();
            topic.setLanguage(language);
            topic.setName(decode(parts[2]));
            topic.setLevel(1);
            return topic;
        }
        return null;
    }

    private Word parseWord(String[] parts) {
        Word word = new Word();
        word.setLanguage(language);
        word.setArticle(decode(parts[ARTICLE_INDEX]));
        word.setWord(decode(parts[WORD_INDEX]));
        word.setAdditionalInformation(decode(parts[ADDITIONAL_INFORMATION_INDEX]));
        word.setTranslations(parseTranslations(parts[TRANSLATIONS_INDEX]));
        word.setTopics(parseTopics(parts[TOPICS_INDEX]));
        return word;
    }

    private List<Translation> parseTranslations(String part) {
        List<Translation> translations = new ArrayList<>();
        String[] parts = part.split(WordExporter.PARTS_DIVIDER);
        Translation translation;
        for (String string : parts) {
            translation = parseTranslation(string);
            if (translation != null) {
                translations.add(translation);
            }
        }
        return translations;
    }

    private Translation parseTranslation(String string) {
        if (string.isEmpty()) {
            return null;
        }
        String[] parts = string.split(WordExporter.ELEMENT_DIVIDER);
        if (parts.length != TRANSLATION_PARTS) {
            LOGGER.info("Wrong translation format " + string);
            return null;
        }
        Translation translation = new Translation();
        translation.setLanguage(parts[TRANSLATION_LANGUAGE_INDEX]);
        translation.setTranslation(decode(parts[TRANSLATION_TRANSLATION_INDEX]));
        return translation;
    }

    private List<Topic> parseTopics(String part) {
        List<Topic> topics = new ArrayList<>();
        String[] parts = part.split(WordExporter.PARTS_DIVIDER);
        Topic topic;
        for (String string : parts) {
            topic = parseTopic(string);
            if (topic != null) {
                topics.add(topic);
            }
        }
        return topics;
    }

    private Topic parseTopic(String string) {
        if (string.isEmpty()) {
            return null;
        }
        String[] parts = string.split(WordExporter.ELEMENT_DIVIDER);
        if (parts.length != TOPIC_PARTS) {
            LOGGER.info("Wrong topic format " + string);
            return null;
        }
        Topic topic = new Topic();
        topic.setLanguage(language);
        topic.setLevel(Integer.parseInt(parts[TOPIC_LEVEL_INDEX]));
        topic.setName(decode(parts[TOPIC_NAME_WORD_INDEX]));
        topic.setRoot(rootTopic);
        return topic;
    }
}
