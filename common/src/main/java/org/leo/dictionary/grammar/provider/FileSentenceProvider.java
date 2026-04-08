package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.config.entity.ParseSentences;
import org.leo.dictionary.entity.Sentence;
import org.leo.dictionary.entity.SentenceCriteria;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.word.provider.FileWordProvider;
import org.leo.dictionary.word.provider.WordExporter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.leo.dictionary.word.provider.FileWordProvider.decode;
import static org.leo.dictionary.word.provider.FileWordProvider.encode;

public class FileSentenceProvider implements SentenceProvider {
    private final static Logger LOGGER = Logger.getLogger(FileSentenceProvider.class.getName());
    public static final String PARSE_SENTENCES_CONFIGURATION = "org.leo.dictionary.config.entity.ParseSentences";

    protected ParseSentences configuration;
    protected Set<Topic> topics;

    protected Topic rootTopic;

    protected Topic topic;
    protected List<Sentence> sentences;

    public void setConfiguration(ParseSentences configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<Sentence> findSentences(SentenceCriteria criteria) {
        if (sentences == null) {
            loadSentences();
        }
        Stream<Sentence> stream = sentences.stream();
        if (criteria.getTopicsOr() != null && !criteria.getTopicsOr().isEmpty()) {
            Set<String> topicNames = criteria.getTopicsOr().stream().map(Topic::getName).collect(Collectors.toSet());
            stream = stream.filter(s -> topicNames.contains(s.getTopic().getName()));
        }
        return stream.collect(Collectors.toList());
    }

    @Override
    public List<Topic> findTopics(String language, Topic rootTopic, int level) {
        if (level > 1) {
            return new ArrayList<>(topics);
        }
        return Collections.singletonList(this.rootTopic);
    }

    @Override
    public List<Topic> findTopics(String language, Set<Topic> rootTopics, int level) {
        return findTopics(language, (Topic) null, level);
    }

    public void loadSentences() {
        try (BufferedReader fileReader = getBufferedReader()) {
            topics = new LinkedHashSet<>();
            sentences = new ArrayList<>();
            rootTopic = creteRootTopic();
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (isIgnoredLine(line)) {
                    continue;
                }
                if (isTopicLine(line)) {
                    topic = createTopic(line);
                } else {
                    Sentence sentence = new Sentence();
                    sentence.setLanguage(configuration.getLanguage());
                    String[] parts = line.split(configuration.getDelimiter(), 2);
                    sentence.setSentence(parts[0].trim());
                    sentence.setTopic(topic);
                    parseTranslations(parts, sentence, line);
                    sentences.add(sentence);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseTranslations(String[] parts, Sentence sentence, String line) {
        if (parts.length == 2) {
            String[] translations = parts[1].split(configuration.getTranslationDelimiter());
            if (translations.length == configuration.getLanguagesTo().size()) {
                sentence.setTranslations(new ArrayList<>(translations.length));
                for (int i = 0; i < translations.length; i++) {
                    Translation translation = new Translation();
                    translation.setTranslation(translations[i].trim());
                    translation.setLanguage(configuration.getLanguagesTo().get(i));
                    sentence.getTranslations().add(translation);
                }
            } else {
                LOGGER.info("Wrong translations quantity in: \"" + line + "\"");
            }
        }
    }

    private Topic creteRootTopic() {
        if (configuration.getRootTopic() != null && !configuration.getRootTopic().isEmpty()) {
            return createTopic(1, configuration.getRootTopic());
        } else {
            return null;
        }
    }

    protected boolean isIgnoredLine(String line) {
        return line == null || line.isEmpty() || isConfigurationLine(line);
    }


    protected BufferedReader getBufferedReader() throws IOException {
        return new BufferedReader(new FileReader(configuration.getPath(), StandardCharsets.UTF_8));
    }

    protected Topic createTopic(String line) {
        int level = 2;
        String name = line.replaceFirst("^" + configuration.getTopicFlag(), "").trim();
        Optional<Topic> foundTopic = topics.stream().filter(t -> Objects.equals(name, t.getName())).findFirst();
        if (foundTopic.isPresent()) {
            return foundTopic.get();
        }
        Topic topic = createTopic(level, name);
        topic.setRoot(rootTopic);
        topics.add(topic);
        return topic;
    }


    private Topic createTopic(int level, String name) {
        Topic topic = new Topic();
        topic.setLanguage(configuration.getLanguage());
        topic.setLevel(level);
        topic.setName(name);
        return topic;
    }

    private boolean isTopicLine(String line) {
        return line.matches("^" + configuration.getTopicFlag() + ".+");
    }

    @Override
    public List<String> languages() {
        return Collections.singletonList(configuration.getLanguage());
    }


    private static boolean isConfigurationLine(String line) {
        return line.startsWith(PARSE_SENTENCES_CONFIGURATION);
    }

    public void parseAndUpdateConfiguration() {
        try (BufferedReader fileReader = getBufferedReader()) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (isConfigurationLine(line)) {
                    String[] configParts = line.split(WordExporter.MAIN_DIVIDER, -1);
                    if (configParts.length < 6) {
                        throw new IllegalArgumentException("config incorrect " + line);
                    }
                    configuration.setLanguage(decode(configParts[1]));
                    configuration.setLanguagesTo(FileWordProvider.parseListProperty(configParts[2]));
                    configuration.setDelimiter(decode(configParts[3]));
                    configuration.setTranslationDelimiter(decode(configParts[4]));
                    configuration.setTopicFlag(decode(configParts[5]));
                    configuration.setRootTopic(decode(configParts[6]));
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String configurationToLine() {
        return String.join(WordExporter.MAIN_DIVIDER, new String[]{
                PARSE_SENTENCES_CONFIGURATION,
                encode(configuration.getLanguage()),
                FileWordProvider.listPropertyToString(configuration.getLanguagesTo()),
                encode(configuration.getDelimiter()),
                encode(configuration.getTranslationDelimiter()),
                encode(configuration.getTopicFlag()),
                encode(configuration.getRootTopic())
        });
    }
}
