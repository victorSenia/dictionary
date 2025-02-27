package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.entity.Sentence;
import org.leo.dictionary.entity.SentenceCriteria;
import org.leo.dictionary.entity.Topic;

import java.util.List;
import java.util.Set;

public interface SentenceProvider {
    List<Sentence> findSentences(SentenceCriteria criteria);

    List<Topic> findTopics(String language, Topic rootTopic, int level);

    List<Topic> findTopics(String language, Set<Topic> rootTopics, int level);

    List<String> languages();
}

