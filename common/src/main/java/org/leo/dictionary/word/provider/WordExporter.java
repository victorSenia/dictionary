package org.leo.dictionary.word.provider;

import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * format of word
 * <pre>
 * {@code
 * article;word;additionalInformation;translations;topics
 *
 * translations
 * language=translation;language=translation;
 *
 * topics
 * level=name;level=name;
 * }
 * </pre>
 */
public abstract class WordExporter {
    public static final String MAIN_DIVIDER = ":";
    public static final String CONFIGURATION_PREFIX = "CONFIGURATION_PREFIX";
    public static final String PARTS_DIVIDER = ";";
    public static final String ELEMENT_DIVIDER = "=";
    private final static Logger LOGGER = Logger.getLogger(WordExporter.class.getName());
    private BufferedWriter writer;
    private String rootTopicName;

    public static boolean notEmptyString(String string) {
        return string != null && !string.isEmpty();
    }

    public static String encode(String string) {
        if (notEmptyString(string)) {
            return URLEncoder.encode(string, StandardCharsets.UTF_8);
        }
        return "";
    }

    public void writeWords(List<Word> list, boolean allForLanguage, List<String> rootTopicNames) throws IOException {
        try (BufferedWriter writer = getBufferedWriter()) {
            this.writer = writer;
            if (allForLanguage) {
                writeWordsForTopic(list, null);
                for (String currentRoot : rootTopicNames) {
                    writeWordsForTopic(list, currentRoot);
                }
            } else {
                if (rootTopicNames.isEmpty()) {
                    writeWordsForTopic(list, null);//no topics
                } else if (rootTopicNames.size() == 1) {
                    for (String currentRoot : rootTopicNames) {
                        writeWordsForTopic(list, currentRoot);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info(e.toString());
            throw e;
        }
    }

    private void writeWordsForTopic(List<Word> list, String rootTopicName) throws IOException {
        this.rootTopicName = rootTopicName;
        writeConfiguration(list.get(0).getLanguage(), rootTopicName);
        for (Word word : list) {
            if (isRelevantForTopic(word)) {
                writeWord(word);
            }
        }
    }

    private boolean isRelevantForTopic(Word word) {
        return (rootTopicName == null && (word.getTopics() == null || word.getTopics().isEmpty() || word.getTopics().stream().anyMatch(this::isRelevantForTopic))) ||
                rootTopicName != null && word.getTopics() != null && word.getTopics().stream().anyMatch(this::isRelevantForTopic);
    }

    protected void writeConfiguration(String language, String rootTopicName) throws IOException {
        writer.write(CONFIGURATION_PREFIX);
        writer.write(MAIN_DIVIDER);
        writer.write(language);
        if (rootTopicName != null) {
            writer.write(MAIN_DIVIDER);
            writer.write(encode(rootTopicName));
        }
        writer.write(System.lineSeparator());
    }

    protected abstract BufferedWriter getBufferedWriter() throws IOException;

    private void writeWord(Word word) throws IOException {
        writer.write(encode(word.getArticle()));
        writer.write(MAIN_DIVIDER);
        writer.write(encode(word.getWord()));
        writer.write(MAIN_DIVIDER);
        writer.write(encode(word.getAdditionalInformation()));
        writer.write(MAIN_DIVIDER);
        writeTranslations(word.getTranslations());
        writer.write(MAIN_DIVIDER);
        writeTopics(word.getTopics());
        writer.write(System.lineSeparator());
    }

    private void writeTopics(List<Topic> topics) throws IOException {
        for (Topic topic : topics) {
            if (isRelevantForTopic(topic)) {
                writeTopic(topic);
                writer.write(PARTS_DIVIDER);
            }
        }
    }

    private boolean isRelevantForTopic(Topic topic) {
        return (rootTopicName == null && topic.getRoot() == null) ||
                (rootTopicName != null && topic.getRoot() != null && Objects.equals(rootTopicName, topic.getRoot().getName()));
    }

    private void writeTopic(Topic topic) throws IOException {
        writer.write(Integer.toString(topic.getLevel()));
        writer.write(ELEMENT_DIVIDER);
        writer.write(encode(topic.getName()));
    }

    private void writeTranslations(List<Translation> translations) throws IOException {
        for (Translation translation : translations) {
            writeTranslation(translation);
            writer.write(PARTS_DIVIDER);
        }
    }

    private void writeTranslation(Translation translation) throws IOException {
        writer.write(translation.getLanguage());
        writer.write(ELEMENT_DIVIDER);
        writer.write(encode(translation.getTranslation()));
    }
}
