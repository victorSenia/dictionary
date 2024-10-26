package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.entity.GrammarCriteria;
import org.leo.dictionary.entity.GrammarSentence;
import org.leo.dictionary.entity.Hint;
import org.leo.dictionary.entity.Topic;

import java.util.List;
import java.util.Set;

public interface GrammarProvider {
    List<GrammarSentence> findSentences(GrammarCriteria criteria);

    List<Hint> findHints(String language, String rootTopic, Set<String> topics);

    List<Topic> findTopics(String language, String rootTopic, int level);

    List<String> languages();
}
