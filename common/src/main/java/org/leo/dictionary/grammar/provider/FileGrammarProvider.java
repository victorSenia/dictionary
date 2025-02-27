package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.config.entity.ParseGrammar;
import org.leo.dictionary.entity.GrammarCriteria;
import org.leo.dictionary.entity.GrammarSentence;
import org.leo.dictionary.entity.Hint;
import org.leo.dictionary.entity.Topic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGrammarProvider implements GrammarProvider {
    private final static Logger LOGGER = Logger.getLogger(FileGrammarProvider.class.getName());

    protected ParseGrammar configuration;
    protected Set<Hint> hints;
    protected Set<Topic> topics;
    protected Hint hint;

    protected Topic rootTopic;

    protected Topic topic;
    protected List<GrammarSentence> sentences;

    public void setConfiguration(ParseGrammar configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<GrammarSentence> findSentences(GrammarCriteria criteria) {
        if (sentences == null) {
            loadSentences();
        }
        Stream<GrammarSentence> stream = sentences.stream();
        if (criteria.getHints() != null && !criteria.getHints().isEmpty()) {
            Set<String> hintNames = criteria.getHints().stream().map(Hint::getHint).collect(Collectors.toSet());
            stream = stream.filter(s -> hintNames.contains(s.getHint().getHint()));
        } else if (criteria.getTopicsOr() != null && !criteria.getTopicsOr().isEmpty()) {
            Set<String> topicNames = criteria.getTopicsOr().stream().map(Topic::getName).collect(Collectors.toSet());
            stream = stream.filter(s -> topicNames.contains(s.getHint().getTopic().getName()));
        }
        return stream.collect(Collectors.toList());
    }

    @Override
    public List<Hint> findHints(String language, Topic rootTopic, Set<Topic> topics) {
        if (topics != null && !topics.isEmpty()) {
            Set<String> topicNames = topics.stream().map(Topic::getName).collect(Collectors.toSet());
            return hints.stream().filter(h -> topicNames.contains(h.getTopic().getName())).collect(Collectors.toList());
        }
        return new ArrayList<>(hints);
    }

    @Override
    public List<Hint> findHints(String language, Set<Topic> rootTopics, Set<Topic> topics) {
        return findHints(language, (Topic) null, topics);
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
            hints = new LinkedHashSet<>();
            sentences = new ArrayList<>();
            rootTopic = creteRootTopic();
            String line;
            String regex = "^(.*)" + configuration.getPlaceholder() + "(.*)" + configuration.getDelimiter() + "(" +
                    (!configuration.getHintAtSameLine() ? ".*" : ".*" + configuration.getDelimiter() + "(.*)") +
                    ")$";
            Pattern pattern = Pattern.compile(regex);
            while ((line = fileReader.readLine()) != null) {
                if (isIgnoredLine(line)) {
                    continue;
                }
                if (isTopicLine(line)) {
                    topic = createTopic(line);
                } else if (isHintLine(line)) {
                    hint = createHint(line);
                } else {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        GrammarSentence sentence = new GrammarSentence();
                        sentence.setSentencePrefix(matcher.group(1).trim());
                        sentence.setSentenceSuffix(matcher.group(2).trim());
                        sentence.setLanguage(configuration.getLanguage());
                        if (!configuration.getHintAtSameLine()) {
                            sentence.setHint(hint);
                            sentence.setAnswer(matcher.group(3).trim());
                            addIfNotPresent(hint.getVariants(), Collections.singletonList(sentence.getAnswer()));
                        } else {
                            sentence.setHint(createHint(matcher.group(3)));
                            sentence.setAnswer(matcher.group(3).split(configuration.getDelimiter())[1].trim());
                        }
                        sentences.add(sentence);
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
        return line == null || line.isEmpty();
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

    protected Hint createHint(String line) {
        String[] parts = line.replaceFirst("^" + configuration.getHintFlag(), "").trim().split(configuration.getDelimiter(), 2);
        Optional<Hint> foundHint = hints.stream().filter(h -> Objects.equals(parts[0], h.getHint()) && h.getTopic() == topic).findFirst();
        Hint hint;
        if (foundHint.isPresent()) {
            hint = foundHint.get();
            addIfNotPresent(hint.getVariants(), parseVariants(parts));
        } else {
            hint = new Hint();
            hint.setHint(parts[0]);
            hint.setLanguage(configuration.getLanguage());
            hint.setTopic(topic);
            hint.setVariants(parseVariants(parts));
            hints.add(hint);
        }
        return hint;
    }

    private List<String> parseVariants(String[] parts) {
        if (parts.length < 2) {
            return new ArrayList<>();
        }
        return Arrays.stream(parts[1].split(configuration.getDelimiter())).map(String::trim).collect(Collectors.toList());
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


    private boolean isHintLine(String line) {
        return line.matches("^" + configuration.getHintFlag() + ".+");
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

    @Override
    public List<String> languages() {
        return Collections.singletonList(configuration.getLanguage());
    }
}
