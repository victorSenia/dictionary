package org.leo.dictionary.config.entity;

import java.util.ArrayList;
import java.util.List;

public class ParseSentences extends ConfigParent {
    private final String language = "";
    private final String path = "";
    private final String rootTopic = "";
    private final String topicFlag = "\t";
    private final List<String> languagesTo = new ArrayList<>();
    private final String translationDelimiter = "\\|";
    private final String delimiter = "\\|";

    public String getLanguage() {
        return getOrDefault("language", language);
    }

    public void setLanguage(String language) {
        put("language", language);
    }

    public String getTopicFlag() {
        return getOrDefault("topicFlag", topicFlag);
    }

    public void setTopicFlag(String topicFlag) {
        put("topicFlag", topicFlag);
    }

    public String getPath() {
        return getOrDefault("path", path);
    }

    public void setPath(String path) {
        put("path", path);
    }

    public String getDelimiter() {
        return getOrDefault("delimiter", delimiter);
    }

    public void setDelimiter(String delimiter) {
        put("delimiter", delimiter);
    }

    public String getRootTopic() {
        return getOrDefault("rootTopic", rootTopic);
    }

    public void setRootTopic(String rootTopic) {
        put("rootTopic", rootTopic);
    }

    public List<String> getLanguagesTo() {
        return getOrDefault("languagesTo", languagesTo);
    }

    public void setLanguagesTo(List<String> languagesTo) {
        put("languagesTo", languagesTo);
    }

    public String getTranslationDelimiter() {
        return getOrDefault("translationDelimiter", translationDelimiter);
    }

    public void setTranslationDelimiter(String translationDelimiter) {
        put("translationDelimiter", translationDelimiter);
    }
}
