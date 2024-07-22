package org.leo.dictionary.word.provider;

import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestWordProvider implements WordProvider {
    @Override
    public List<Word> findWords(WordCriteria wordCriteria) {
        Word word = new Word();
        word.setWord("setWord");
        word.setLanguage("setLanguage");
        Translation translation = new Translation();
        translation.setTranslation("setTranslation");
        translation.setLanguage("setLanguageTranslation");
        word.setTranslations(Collections.singletonList(translation));
        List<Word> words = new ArrayList<>();
        words.add(word);
        return words;
    }

    @Override
    public List<Topic> findTopicsWithRoot(String language, String rootTopic, int upToLevel) {
        return null;
    }

    @Override
    public List<Topic> findTopics(String language, int upToLevel) {
        return null;
    }

    @Override
    public List<String> languageFrom() {
        return null;
    }

    @Override
    public List<String> languageTo(String language) {
        return null;
    }

    @Override
    public List<Word> findKnownWords() {
        return new ArrayList<>();
    }

}
