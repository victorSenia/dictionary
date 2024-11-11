package org.leo.dictionary;

import org.leo.dictionary.entity.Word;

import java.util.List;
import java.util.Set;

public interface PlayService {
    void play();

    void pause();

    void setPlayTranslationFor(Set<String> playTranslationFor);

    void next();

    void playFrom(int index);

    boolean isPlaying();

    void previous();

    void setUiUpdater(UiUpdater uiUpdater);

    void setWords(List<Word> words);
}
