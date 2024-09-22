package org.leo.dictionary;

import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.util.List;
import java.util.stream.Collectors;

public interface ExternalWordProvider {

    List<Word> findWords(WordCriteria criteria);

    default int countWords(WordCriteria wordCriteria) {
        return findWords(wordCriteria).size();
    }

    default List<String> findTopics(String language) {
        return findTopics(language, 2).stream().map(Topic::getName).sorted().collect(Collectors.toList());
    }

    List<Topic> findTopicsWithRoot(String language, String rootTopic, int upToLevel);

    List<Topic> findTopics(String language, int level);

    List<String> languageFrom();

    List<String> languageTo(String language);
}
