package org.leo.dictionary.entity;

import java.io.Serializable;
import java.util.Set;

public class SentenceCriteria implements Serializable {
    private String language;
    private Set<String> hints;
    private Set<String> topicsOr;
    private String rootTopic;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<String> getHints() {
        return hints;
    }

    public void setHints(Set<String> hints) {
        this.hints = hints;
    }

    public Set<String> getTopicsOr() {
        return topicsOr;
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
}
