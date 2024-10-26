package org.leo.dictionary.grammar.provider;

import org.leo.dictionary.entity.Sentence;
import org.leo.dictionary.entity.SentenceCriteria;
import org.leo.dictionary.entity.Topic;

import java.util.List;

public interface SentenceProvider {
    List<Sentence> findSentences(SentenceCriteria criteria);

    List<Topic> findTopics(String language, String rootTopic, int level);

    List<String> languages();
}

