package org.leo.dictionary.entity;

import java.io.Serializable;
import java.util.Set;

public class SentenceCriteria implements Serializable {
    private String language;
    private Set<Topic> topicsOr;
    private Set<Topic> rootTopics;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<Topic> getTopicsOr() {
        return topicsOr;
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
}
