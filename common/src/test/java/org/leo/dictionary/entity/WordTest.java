package org.leo.dictionary.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordTest {

    @Test
    void formatWordReturnsExpectedText() {
        Word word = new Word();
        word.setArticle("das ");
        word.setWord("Haus");
        word.setAdditionalInformation("noun");
        Translation t = new Translation();
        t.setLanguage("en");
        t.setTranslation("house");
        word.setTranslations(List.of(t));

        assertEquals("das Haus,noun - house", Word.formatWord(word));
    }

    @Test
    void formatWordReturnsEmptyStringForNullInput() {
        assertEquals("", Word.formatWord(null));
    }

    @Test
    void updateWordFiltersTranslationsByRequestedLanguages() {
        Word current = new Word();
        current.setWord("old");
        Word incoming = new Word();
        incoming.setWord("new");
        incoming.setLanguage("de");
        incoming.setArticle("die ");
        incoming.setAdditionalInformation("info");

        Translation en = new Translation();
        en.setLanguage("en");
        en.setTranslation("new");
        Translation es = new Translation();
        es.setLanguage("es");
        es.setTranslation("nuevo");
        incoming.setTranslations(List.of(en, es));

        current.updateWord(incoming, Set.of("en"));

        assertEquals("new", current.getWord());
        assertEquals("de", current.getLanguage());
        assertEquals("die ", current.getArticle());
        assertEquals("info", current.getAdditionalInformation());
        assertEquals(1, current.getTranslations().size());
        assertEquals("en", current.getTranslations().get(0).getLanguage());
    }

    @Test
    void updateWordKeepsAllTranslationsWhenLanguageFilterIsNull() {
        Word current = new Word();
        Word incoming = new Word();
        incoming.setWord("new");
        incoming.setLanguage("de");

        Translation en = new Translation();
        en.setLanguage("en");
        en.setTranslation("new");
        Translation es = new Translation();
        es.setLanguage("es");
        es.setTranslation("nuevo");
        incoming.setTranslations(List.of(en, es));

        current.updateWord(incoming, null);

        assertEquals(2, current.getTranslations().size());
    }

    @Test
    void updateWordKeepsAllTranslationsWhenLanguageFilterIsEmpty() {
        Word current = new Word();
        Word incoming = new Word();
        incoming.setWord("new");
        incoming.setLanguage("de");

        Translation en = new Translation();
        en.setLanguage("en");
        en.setTranslation("new");
        Translation es = new Translation();
        es.setLanguage("es");
        es.setTranslation("nuevo");
        incoming.setTranslations(List.of(en, es));

        current.updateWord(incoming, Collections.emptySet());

        assertEquals(2, current.getTranslations().size());
    }
}
