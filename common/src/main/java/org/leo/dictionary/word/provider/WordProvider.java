package org.leo.dictionary.word.provider;

import org.leo.dictionary.ExternalWordProvider;
import org.leo.dictionary.entity.Word;

import java.util.List;

public interface WordProvider extends ExternalWordProvider {


    List<Word> findKnownWords();

    default void updateWord(Word word) {

    }

    default void updateWords(List<Word> words) {
        for (Word word : words) {
            updateWord(word);
        }
    }
}
