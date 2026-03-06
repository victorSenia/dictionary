package org.leo.dictionary;

import org.junit.jupiter.api.Test;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExternalWordProviderTest {

    @Test
    void countWordsUsesFindWordsForGivenCriteria() {
        AtomicReference<WordCriteria> seenCriteria = new AtomicReference<>();
        ExternalWordProvider provider = new ExternalWordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                seenCriteria.set(criteria);
                return List.of(new Word(), new Word(), new Word());
            }

            @Override
            public List<Topic> findTopicsWithRoot(String language, Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<Topic> findTopicsWithRoot(String language, Set<Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<Topic> findTopics(String language, int level) {
                return List.of();
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }
        };

        WordCriteria criteria = new WordCriteria();
        assertEquals(3, provider.countWords(criteria));
        assertSame(criteria, seenCriteria.get());
    }

    @Test
    void findTopicsDefaultUsesLevelTwoAndReturnsSortedNames() {
        AtomicReference<String> seenLanguage = new AtomicReference<>();
        AtomicInteger seenLevel = new AtomicInteger(-1);
        ExternalWordProvider provider = new ExternalWordProvider() {
            @Override
            public List<Word> findWords(WordCriteria criteria) {
                return List.of();
            }

            @Override
            public List<Topic> findTopicsWithRoot(String language, Topic rootTopic, int upToLevel) {
                return List.of();
            }

            @Override
            public List<Topic> findTopicsWithRoot(String language, Set<Topic> rootTopics, int upToLevel) {
                return List.of();
            }

            @Override
            public List<Topic> findTopics(String language, int level) {
                seenLanguage.set(language);
                seenLevel.set(level);

                Topic animals = new Topic();
                animals.setName("Animals");
                Topic basics = new Topic();
                basics.setName("Basics");
                Topic zoo = new Topic();
                zoo.setName("Zoo");
                return List.of(zoo, basics, animals);
            }

            @Override
            public List<String> languageFrom() {
                return List.of();
            }

            @Override
            public List<String> languageTo(String language) {
                return List.of();
            }
        };

        assertEquals(List.of("Animals", "Basics", "Zoo"), provider.findTopics("de"));
        assertEquals("de", seenLanguage.get());
        assertEquals(2, seenLevel.get());
    }
}
