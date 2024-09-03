package org.leo.dictionary.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordCriteria implements Serializable {
    private Double knowledgeFrom;
    private Double knowledgeTo;
    private Set<String> topicsAnd;
    private Set<String> topicsOr;
    private String rootTopic;
    private String languageFrom;
    private Set<String> languageTo;
    private Set<String> playTranslationFor;
    private boolean noRootTopic;
    private boolean noTopic;
    private long shuffleRandom = -1;

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

    public Set<String> getTopicsAnd() {
        return topicsAnd;
    }

    public void setTopicsAnd(List<String> topicsAnd) {
        this.topicsAnd = new HashSet<>(topicsAnd);
    }

    public void setTopicsAnd(Set<String> topicsAnd) {
        this.topicsAnd = topicsAnd;
    }

    public Set<String> getTopicsOr() {
        return topicsOr;
    }

    public void setTopicsOr(List<String> topicsOr) {
        this.topicsOr = new HashSet<>(topicsOr);
    }

    public void setTopicsOr(Set<String> topicsOr) {
        this.topicsOr = topicsOr;
    }

    public String getRootTopic() {
        return rootTopic;
    }

    public void setRootTopic(String rootTopic) {
        this.rootTopic = rootTopic;
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
}
