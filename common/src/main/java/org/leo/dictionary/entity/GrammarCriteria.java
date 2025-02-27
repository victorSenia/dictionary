package org.leo.dictionary.entity;

import java.io.Serializable;
import java.util.Set;

public class GrammarCriteria implements Serializable {
    private String language;
    private Set<Hint> hints;
    private Set<Topic> topicsOr;
    private Set<Topic> rootTopics;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<Hint> getHints() {
        return hints;
    }

    public void setHints(Set<Hint> hints) {
        this.hints = hints;
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
