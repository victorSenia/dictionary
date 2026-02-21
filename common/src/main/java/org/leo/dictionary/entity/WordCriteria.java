package org.leo.dictionary.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordCriteria implements Serializable {
    public static long NOT_SET = -1;
    private Double knowledgeFrom;
    private Double knowledgeTo;
    private Set<Topic> topicsOr;
    private Set<Topic> rootTopics;
    private String languageFrom;
    private Set<String> languageTo;
    private Set<String> playTranslationFor;
    private boolean noRootTopic;
    private boolean noTopic;
    private long shuffleRandom = NOT_SET;
    private WordsOrderMode wordsOrderMode = WordCriteria.WordsOrderMode.SORTED;

    public Double getKnowledgeFrom() {
        return knowledgeFrom;
    }

    public void setKnowledgeFrom(double knowledgeFrom) {
        this.knowledgeFrom = knowledgeFrom;
    }

    public void setKnowledgeFrom(Double knowledgeFrom) {
        this.knowledgeFrom = knowledgeFrom;
    }

    public Double getKnowledgeTo() {
        return knowledgeTo;
    }

    public void setKnowledgeTo(double knowledgeTo) {
        this.knowledgeTo = knowledgeTo;
    }

    public void setKnowledgeTo(Double knowledgeTo) {
        this.knowledgeTo = knowledgeTo;
    }

    public Set<Topic> getTopicsOr() {
        return topicsOr;
    }

    public void setTopicsOr(List<Topic> topicsOr) {
        this.topicsOr = new HashSet<>(topicsOr);
    }

    public void setTopicsOr(Set<Topic> topicsOr) {
        this.topicsOr = topicsOr;
    }

    public Set<Topic> getRootTopics() {
        return rootTopics;
    }

    public void setRootTopics(Set<Topic> rootTopics) {
        this.rootTopics = rootTopics;
    }

    public String getLanguageFrom() {
        return languageFrom;
    }

    public void setLanguageFrom(String languageFrom) {
        this.languageFrom = languageFrom;
    }

    public Set<String> getLanguageTo() {
        return languageTo;
    }

    public void setLanguageTo(Set<String> languageTo) {
        this.languageTo = languageTo;
    }

    public boolean isNoRootTopic() {
        return noRootTopic;
    }

    public void setNoRootTopic(boolean noRootTopic) {
        this.noRootTopic = noRootTopic;
    }

    public boolean isNoTopic() {
        return noTopic;
    }

    public void setNoTopic(boolean noTopic) {
        this.noTopic = noTopic;
    }

    public Set<String> getPlayTranslationFor() {
        if (playTranslationFor != null && !playTranslationFor.isEmpty()) {
            return playTranslationFor;
        }
        return languageTo;
    }

    public void setPlayTranslationFor(Set<String> playTranslationFor) {
        this.playTranslationFor = playTranslationFor;
    }

    public long getShuffleRandom() {
        return shuffleRandom;
    }

    public void setShuffleRandom(long shuffleRandom) {
        this.shuffleRandom = shuffleRandom;
    }

    public WordsOrderMode getWordsOrderMode() {
        return wordsOrderMode;
    }

    public void setWordsOrderMode(WordsOrderMode wordsOrderMode) {
        this.wordsOrderMode = wordsOrderMode;
    }

    public enum WordsOrderMode {
        IMPORT_ORDER,
        SORTED,
        SHUFFLE

    }
}
