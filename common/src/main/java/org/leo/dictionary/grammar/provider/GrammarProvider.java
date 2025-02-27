package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.entity.GrammarCriteria;
import org.leo.dictionary.entity.GrammarSentence;
import org.leo.dictionary.entity.Hint;
import org.leo.dictionary.entity.Topic;

import java.util.List;
import java.util.Set;

public interface GrammarProvider {
    List<GrammarSentence> findSentences(GrammarCriteria criteria);

    List<Hint> findHints(String language, Topic rootTopic, Set<Topic> topics);

    List<Hint> findHints(String language, Set<Topic> rootTopics, Set<Topic> topics);

    List<Topic> findTopics(String language, Topic rootTopic, int level);

    List<Topic> findTopics(String language, Set<Topic> rootTopics, int level);

    List<String> languages();
}
