package org.leo.dictionary.word.provider;

import org.leo.dictionary.ExternalWordProvider;
import org.leo.dictionary.entity.Word;

import java.util.List;

public interface WordProvider extends ExternalWordProvider, WordUpdater {

    List<Word> findKnownWords();
}
