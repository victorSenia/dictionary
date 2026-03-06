package org.leo.dictionary.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WordCriteriaTest {

    @Test
    void getPlayTranslationForFallsBackToLanguageToWhenExplicitValueMissing() {
        WordCriteria criteria = new WordCriteria();
        criteria.setLanguageTo(Set.of("en", "es"));

        assertEquals(Set.of("en", "es"), criteria.getPlayTranslationFor());
    }

    @Test
    void getPlayTranslationForPrefersExplicitValueWhenPresent() {
        WordCriteria criteria = new WordCriteria();
        criteria.setLanguageTo(Set.of("en", "es"));
        criteria.setPlayTranslationFor(Set.of("de"));

        assertEquals(Set.of("de"), criteria.getPlayTranslationFor());
    }

    @Test
    void getPlayTranslationForReturnsNullWhenNothingIsConfigured() {
        WordCriteria criteria = new WordCriteria();

        assertNull(criteria.getPlayTranslationFor());
    }
}
