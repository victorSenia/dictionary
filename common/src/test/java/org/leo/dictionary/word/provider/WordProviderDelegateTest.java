package org.leo.dictionary.word.provider;

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

class WordProviderDelegateTest {

    @Test
    void allCallsAreForwardedToConfiguredDelegate() {
        StubWordProvider stub = new StubWordProvider();
        WordProviderDelegate delegate = new WordProviderDelegate();
        delegate.setWordProvider(stub);

        WordCriteria criteria = new WordCriteria();
        Word word = new Word();
        List<Word> words = List.of(word);
        Topic root = new Topic();
        root.setName("Root");
        Set<Topic> roots = Set.of(root);

        assertSame(stub, delegate.getDelegate());
        assertEquals(stub.knownWords, delegate.findKnownWords());

        delegate.updateWord(word);
        assertSame(word, stub.lastUpdatedWord.get());

        delegate.updateWords(words);
        assertSame(words, stub.lastUpdatedWords.get());

        assertEquals(stub.words, delegate.findWords(criteria));
        assertSame(criteria, stub.lastFindWordsCriteria.get());

        assertEquals(7, delegate.countWords(criteria));
        assertSame(criteria, stub.lastCountCriteria.get());

        assertEquals(stub.topicNames, delegate.findTopics("de"));
        assertEquals("de", stub.lastFindTopicsLanguage.get());

        assertEquals(stub.topicsForRoot, delegate.findTopicsWithRoot("de", root, 3));
        assertEquals("de", stub.lastRootLanguage.get());
        assertSame(root, stub.lastRootTopic.get());
        assertEquals(3, stub.lastRootLevel.get());

        assertEquals(stub.topicsForRoots, delegate.findTopicsWithRoot("de", roots, 4));
        assertEquals("de", stub.lastRootsLanguage.get());
        assertSame(roots, stub.lastRootTopics.get());
        assertEquals(4, stub.lastRootsLevel.get());

        assertEquals(stub.topicsForLevel, delegate.findTopics("de", 2));
        assertEquals("de", stub.lastTopicsLevelLanguage.get());
        assertEquals(2, stub.lastTopicsLevel.get());

        assertEquals(stub.languagesFrom, delegate.languageFrom());
        assertEquals(stub.languagesTo, delegate.languageTo("de"));
        assertEquals("de", stub.lastLanguageToLanguage.get());
    }

    private static class StubWordProvider implements WordProvider {
        private final List<Word> knownWords = List.of(new Word());
        private final List<Word> words = List.of(new Word());
        private final List<String> topicNames = List.of("Animals");
        private final List<Topic> topicsForRoot = List.of(new Topic());
        private final List<Topic> topicsForRoots = List.of(new Topic());
        private final List<Topic> topicsForLevel = List.of(new Topic());
        private final List<String> languagesFrom = List.of("de");
        private final List<String> languagesTo = List.of("en", "es");

        private final AtomicReference<Word> lastUpdatedWord = new AtomicReference<>();
        private final AtomicReference<List<Word>> lastUpdatedWords = new AtomicReference<>();
        private final AtomicReference<WordCriteria> lastFindWordsCriteria = new AtomicReference<>();
        private final AtomicReference<WordCriteria> lastCountCriteria = new AtomicReference<>();
        private final AtomicReference<String> lastFindTopicsLanguage = new AtomicReference<>();
        private final AtomicReference<String> lastRootLanguage = new AtomicReference<>();
        private final AtomicReference<Topic> lastRootTopic = new AtomicReference<>();
        private final AtomicInteger lastRootLevel = new AtomicInteger();
        private final AtomicReference<String> lastRootsLanguage = new AtomicReference<>();
        private final AtomicReference<Set<Topic>> lastRootTopics = new AtomicReference<>();
        private final AtomicInteger lastRootsLevel = new AtomicInteger();
        private final AtomicReference<String> lastTopicsLevelLanguage = new AtomicReference<>();
        private final AtomicInteger lastTopicsLevel = new AtomicInteger();
        private final AtomicReference<String> lastLanguageToLanguage = new AtomicReference<>();

        @Override
        public List<Word> findKnownWords() {
            return knownWords;
        }

        @Override
        public void updateWord(Word word) {
            lastUpdatedWord.set(word);
        }

        @Override
        public void updateWords(List<Word> words) {
            lastUpdatedWords.set(words);
        }

        @Override
        public List<Word> findWords(WordCriteria criteria) {
            lastFindWordsCriteria.set(criteria);
            return words;
        }

        @Override
        public int countWords(WordCriteria wordCriteria) {
            lastCountCriteria.set(wordCriteria);
            return 7;
        }

        @Override
        public List<String> findTopics(String language) {
            lastFindTopicsLanguage.set(language);
            return topicNames;
        }

        @Override
        public List<Topic> findTopicsWithRoot(String language, Topic rootTopic, int upToLevel) {
            lastRootLanguage.set(language);
            lastRootTopic.set(rootTopic);
            lastRootLevel.set(upToLevel);
            return topicsForRoot;
        }

        @Override
        public List<Topic> findTopicsWithRoot(String language, Set<Topic> rootTopics, int upToLevel) {
            lastRootsLanguage.set(language);
            lastRootTopics.set(rootTopics);
            lastRootsLevel.set(upToLevel);
            return topicsForRoots;
        }

        @Override
        public List<Topic> findTopics(String language, int level) {
            lastTopicsLevelLanguage.set(language);
            lastTopicsLevel.set(level);
            return topicsForLevel;
        }

        @Override
        public List<String> languageFrom() {
            return languagesFrom;
        }

        @Override
        public List<String> languageTo(String language) {
            lastLanguageToLanguage.set(language);
            return languagesTo;
        }
    }
}
