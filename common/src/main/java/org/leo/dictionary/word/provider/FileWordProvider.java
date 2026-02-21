package org.leo.dictionary.word.provider;

import org.leo.dictionary.config.entity.ParseWords;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileWordProvider implements WordProvider {
    private final static Logger LOGGER = Logger.getLogger(FileWordProvider.class.getName());

    public static final String PARSE_WORDS_CONFIGURATION = "org.leo.dictionary.config.entity.ParseWords";

    protected ParseWords configuration;
    protected Map<String, Topic> topics;
    protected Topic rootTopic;
    protected List<Word> words;

    public void setConfiguration(ParseWords configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<Word> findWords(WordCriteria wordCriteria) {
        if (words == null) {
            loadWords();
        }
        Stream<Word> stream = words.stream();
        if (wordCriteria.getTopicsOr() != null && !wordCriteria.getTopicsOr().isEmpty()) {
            Set<String> topicsNames = wordCriteria.getTopicsOr().stream().map(Topic::getName).collect(Collectors.toSet());
            stream = stream.filter(word -> word.getTopics().stream().map(Topic::getName).anyMatch(topicsNames::contains));
        }
        if (wordCriteria.getKnowledgeFrom() != null) {
            double expectedKnowledge = wordCriteria.getKnowledgeFrom();
            stream = stream.filter(word -> word.getKnowledge() >= expectedKnowledge);
        }
        if (wordCriteria.getKnowledgeTo() != null) {
            double expectedKnowledge = wordCriteria.getKnowledgeTo();
            stream = stream.filter(word -> word.getKnowledge() < expectedKnowledge);
        }
        WordCriteria.WordsOrderMode mode = wordCriteria.getWordsOrderMode();
        if (mode == null) {
            mode = wordCriteria.getShuffleRandom() != WordCriteria.NOT_SET
                    ? WordCriteria.WordsOrderMode.SHUFFLE
                    : WordCriteria.WordsOrderMode.IMPORT_ORDER;
        }
        if (mode == WordCriteria.WordsOrderMode.SORTED) {
            stream = stream.sorted(
                    Comparator.comparing(Word::getLanguage, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(Word::getWord, String.CASE_INSENSITIVE_ORDER)
            );
        }
        List<Word> result = stream.collect(Collectors.toList());
        if (mode == WordCriteria.WordsOrderMode.SHUFFLE) {
            long seed = wordCriteria.getShuffleRandom() == WordCriteria.NOT_SET
                    ? System.currentTimeMillis()
                    : wordCriteria.getShuffleRandom();
            Collections.shuffle(result, new Random(seed));
        }
        return result;
    }

    @Override
    public List<Topic> findTopics(String language, int upToLevel) {
        if (topics == null) {
            loadWords();
        }
        if (upToLevel > 2) {
            return new ArrayList<>(topics.values());
        }
        return topics.values().stream().filter(topic -> topic.getLevel() == upToLevel).collect(Collectors.toList());
    }

    @Override
    public List<Topic> findTopicsWithRoot(String language, Topic rootTopic, int upToLevel) {
        return findTopics(language, upToLevel);
    }

    @Override
    public List<Topic> findTopicsWithRoot(String language, Set<Topic> rootTopics, int upToLevel) {
        return findTopics(language, upToLevel);
    }

    public void loadWords() {
        try (BufferedReader fileReader = getBufferedReader()) {
            topics = new HashMap<>();
            words = new ArrayList<>();
            rootTopic = creteRootTopic();
            String line;
            List<Topic> topicsOfCurrentWors = null;
            int limit = configuration.getLanguagesTo().size() + 1;
            while ((line = fileReader.readLine()) != null) {
                if (isIgnoredLine(line)) {
                    continue;
                }
                if (isTopicLine(line)) {
                    topicsOfCurrentWors = getWordTopics(topicsOfCurrentWors, line);
                } else {
                    String[] parsedWord = line.split(configuration.getDelimiter(), limit);
                    if (parsedWord.length == limit) {
                        Word word = parseWord(parsedWord);
                        Optional<Word> foundWordOptional = words.stream().filter(w -> w.equals(word)).findFirst();
                        if (foundWordOptional.isPresent()) {
                            Word foundWord = foundWordOptional.get();
                            if (foundWord.getTopics() != null) {
                                foundWord.setTopics(new ArrayList<>(foundWord.getTopics()));
                            } else if (topicsOfCurrentWors != null) {
                                foundWord.setTopics(new ArrayList<>());
                            }
                            addIfNotPresent(foundWord.getTopics(), topicsOfCurrentWors);
                            addIfNotPresent(foundWord.getTranslations(), word.getTranslations());
                        } else {
                            word.setTopics(topicsOfCurrentWors);
                            words.add(word);
                        }
                    } else {
                        LOGGER.info("'" + line + "' not added");
                    }
                }
            }
//            wordsToFile(configuration.getPath() + "__", words);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private <V> void addIfNotPresent(List<V> list, List<V> addList) {
        if (addList != null && list != null) {
            for (V element : addList) {
                if (!list.contains(element)) {
                    list.add(element);
                }
            }
        }
    }

    protected BufferedReader getBufferedReader() throws IOException {
        return new BufferedReader(new FileReader(configuration.getPath(), StandardCharsets.UTF_8));
    }

    protected List<Topic> getWordTopics(List<Topic> topics, String line) {
        Topic topic = createTopic(line);
        List<Topic> newTopics;
        if (topics == null || topic.getLevel() == 2) {
            newTopics = new ArrayList<>();
        } else {
            newTopics = new ArrayList<>(topics);
            Topic lastTopic = newTopics.get(newTopics.size() - 1);
            while (lastTopic.getLevel() >= topic.getLevel()) {
                newTopics.remove(newTopics.size() - 1);
                lastTopic = newTopics.get(newTopics.size() - 1);
            }
            if (lastTopic.getLevel() + 1 != topic.getLevel()) {
                LOGGER.severe("Wrong topics levels for topic=" + topic);
            }
        }
        newTopics.add(topic);
        return newTopics;
    }

    protected Topic createTopic(String line) {
        int level = 1;
        String name = line;
        Topic exist = topics.get(line);
        if (exist != null) {
            return exist;
        }
        do {
            name = name.replaceFirst("^" + configuration.getTopicFlag() +
                    (configuration.getTopicDelimiter().isEmpty() ? configuration.getTopicDelimiter() + "?" : ""), "");
            level++;
        } while (isTopicLine(name));
        Topic topic = createTopic(level, name);
        topic.setRoot(rootTopic);
        topics.put(line, topic);
        return topic;
    }

    private Topic createTopic(int level, String name) {
        Topic topic = new Topic();
        topic.setLanguage(configuration.getLanguageFrom());
        topic.setLevel(level);
        topic.setName(name);
        return topic;
    }

    private boolean isTopicLine(String line) {
        return line.matches("^" + configuration.getTopicFlag() + ".+");
    }

    protected Word parseWord(String[] parsedWord) {
        Word word = new Word();
        String trimmedWord = parsedWord[0].trim();
        for (String article : configuration.getArticles()) {
            if (trimmedWord.startsWith(article)) {
                word.setArticle(article);
                trimmedWord = trimmedWord.substring(article.length());
                break;
            }
        }
        String[] additionalInformation = trimmedWord.split(configuration.getAdditionalInformationDelimiter(), 2);
        word.setWord(additionalInformation[0]);
        if (additionalInformation.length > 1) {
            word.setAdditionalInformation(additionalInformation[1]);
        }
        word.setLanguage(configuration.getLanguageFrom());
        List<Translation> translations = new ArrayList<>();
        for (int i = 1; i < parsedWord.length; i++) {
            for (String parsedTranslation : parsedWord[i].split(configuration.getTranslationDelimiter())) {
                Translation translation = createTranslation(parsedTranslation.trim(), configuration.getLanguagesTo().get(i - 1));
                translations.add(translation);
            }
        }
        word.setTranslations(translations);
        return word;
    }


    protected Translation createTranslation(String parsedTranslation, String languagesTo) {
        Translation translation = new Translation();
        translation.setTranslation(parsedTranslation);
        translation.setLanguage(languagesTo);
        return translation;
    }

    protected void wordsToFile(String path, List<Word> words) throws IOException {
        try (BufferedWriter bufferedWriter = getBufferedWriter(path)) {
            Map<String, List<Word>> result = words.stream().collect(Collectors.groupingBy(this::topicsToString, Collectors.mapping(w -> w, Collectors.toList())));
            List<String> topics = result.keySet().stream().sorted().collect(Collectors.toList());
            String previousTopic = null;
            for (String topic : topics) {
                bufferedWriter.write(trimTopic(topic, previousTopic));
                bufferedWriter.newLine();
                previousTopic = topic;
                for (Word word : result.get(topic)) {
                    bufferedWriter.write(wordToString(word));
                    bufferedWriter.newLine();
                }
            }
        }
    }

    protected BufferedWriter getBufferedWriter(String path) throws IOException {
        return new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8));
    }

    protected String trimTopic(String topic, String previousTopic) {
        while (previousTopic != null) {
            if (topic.startsWith(previousTopic + System.lineSeparator())) {
                return topic.substring(previousTopic.length() + System.lineSeparator().length());
            }
            if (previousTopic.contains(System.lineSeparator())) {
                previousTopic = previousTopic.substring(0, previousTopic.lastIndexOf(System.lineSeparator()));
            } else {
                previousTopic = null;
            }
        }
        return topic;
    }

    protected String wordToString(Word word) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(word.getFullWord());
        if (word.getAdditionalInformation() != null) {
            stringBuilder.append(configuration.getAdditionalInformationDelimiter());
            stringBuilder.append(word.getAdditionalInformation());
        }
        for (int index = 0; index < configuration.getLanguagesTo().size(); index++) {
            stringBuilder.append(configuration.getDelimiter().replaceAll("\\\\", ""));
            int finalIndex = index;
            stringBuilder.append(word.getTranslations().stream().filter(t -> t.getLanguage().equals(configuration.getLanguagesTo().get(finalIndex)))
                    .map(Translation::getTranslation).collect(Collectors.joining(configuration.getTranslationDelimiter())));
        }
        return stringBuilder.toString();
    }

    protected String topicsToString(Word word) {
        if (word.getTopics() != null) {
            return word.getTopics().stream().sorted(Comparator.comparingInt(Topic::getLevel)).map(this::topicToString).collect(Collectors.joining(System.lineSeparator()));
        }
        return "";
    }

    protected String topicToString(Topic topic) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < topic.getLevel(); i++) {
            stringBuilder.append(configuration.getTopicFlag()).append(configuration.getTopicDelimiter());
        }
        stringBuilder.append(topic.getName());
        return stringBuilder.toString();
    }

    public static boolean isConfigurationLine(String line) {
        return line.startsWith(PARSE_WORDS_CONFIGURATION);
    }

    public static String decode(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        return WordImporter.decode(string);
    }

    public static List<String> parseListProperty(String configPart) {
        if (configPart.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(configPart.split(WordExporter.PARTS_DIVIDER)).map(FileWordProvider::decode).collect(Collectors.toList());
    }

    public static String encode(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        return WordExporter.encode(string);
    }

    public static String listPropertyToString(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return "";
        }
        return strings.stream().map(FileWordProvider::encode).collect(Collectors.joining(WordExporter.PARTS_DIVIDER));
    }

    public void parseAndUpdateConfiguration() {
        try (BufferedReader fileReader = getBufferedReader()) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (isConfigurationLine(line)) {
                    String[] configParts = line.split(WordExporter.MAIN_DIVIDER, -1);
                    if (configParts.length < 10) {
                        throw new IllegalArgumentException("config incorrect " + line);
                    }
                    configuration.setLanguageFrom(decode(configParts[1]));
                    configuration.setLanguagesTo(parseListProperty(configParts[2]));
                    configuration.setArticles(parseListProperty(configParts[3]));
                    configuration.setDelimiter(decode(configParts[4]));
                    configuration.setAdditionalInformationDelimiter(decode(configParts[5]));
                    configuration.setTranslationDelimiter(decode(configParts[6]));
                    configuration.setTopicFlag(decode(configParts[7]));
                    configuration.setTopicDelimiter(decode(configParts[8]));
                    configuration.setRootTopic(decode(configParts[9]));
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String configurationToLine() {
        return String.join(WordExporter.MAIN_DIVIDER, new String[]{
                PARSE_WORDS_CONFIGURATION,
                encode(configuration.getLanguageFrom()),
                listPropertyToString(configuration.getLanguagesTo()),
                listPropertyToString(configuration.getArticles()),
                encode(configuration.getDelimiter()),
                encode(configuration.getAdditionalInformationDelimiter()),
                encode(configuration.getTranslationDelimiter()),
                encode(configuration.getTopicFlag()),
                encode(configuration.getTopicDelimiter()),
                encode(configuration.getRootTopic())
        });
    }
    @Override
    public List<Word> findKnownWords() {
        return new ArrayList<>();
    }

    @Override
    public List<String> languageFrom() {
        return Collections.singletonList(configuration.getLanguageFrom());
    }

    @Override
    public List<String> languageTo(String language) {
        return configuration.getLanguagesTo();
    }
}
