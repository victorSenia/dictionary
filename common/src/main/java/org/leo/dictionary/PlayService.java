package org.leo.dictionary;

import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.util.List;

public interface PlayService {
    void play();

    void pause();

    void next();

    void playFrom(int index);

    boolean isPlaying();

    void previous();

    void setUiUpdater(UiUpdater uiUpdater);

    void safeDelete(int index);

    void safeUpdate(int index, Word updatedWord);

    void safeAdd(Word addedWord);

    List<Word> findWords(WordCriteria topics);

    List<Word> getUnknownWords();
}
