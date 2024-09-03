package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.entity.Hint;
import org.leo.dictionary.entity.Sentence;
import org.leo.dictionary.entity.SentenceCriteria;
import org.leo.dictionary.entity.Topic;

import java.util.List;
import java.util.Set;

public interface GrammarProvider {
    List<Sentence> findSentences(SentenceCriteria criteria);

    List<Hint> findHints(String language, String rootTopic, Set<String> topics);

    List<Topic> findTopics(String language, String rootTopic, int level);

    List<String> languages();
}
