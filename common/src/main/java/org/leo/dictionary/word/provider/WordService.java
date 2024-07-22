package org.leo.dictionary.word.provider;

import org.leo.dictionary.entity.Word;

public class WordService {
    public void setKnown(Word word) {
        word.setKnowledge(1.);
    }

    public void setUnknown(Word word) {
        word.setKnowledge(0.);
    }

    public void setKnown(Word word, double known) {
        word.setKnowledge(known);
    }
}
