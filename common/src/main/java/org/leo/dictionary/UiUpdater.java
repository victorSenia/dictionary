package org.leo.dictionary;

import org.leo.dictionary.entity.Word;

public interface UiUpdater {
    void updateState(Word word, int index);
}
