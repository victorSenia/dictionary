package org.leo.dictionary.config.entity;

import java.util.ArrayList;
import java.util.List;

public class ParseWords extends ConfigParent {
    private final String languageFrom = "";
    private final List<String> languagesTo = new ArrayList<>();
    private final String delimiter = "\\|";
    private final String additionalInformationDelimiter = ";";
    private final String translationDelimiter = ";";
    private final String topicFlag = "\t";
    private final String topicDelimiter = "";
    private final String path = "";
    private final String rootTopic = "";


    private final List<String> articles = new ArrayList<>();

    public String getLanguageFrom() {
        return getOrDefault("languageFrom", languageFrom);
    }

    public void setLanguageFrom(String languageFrom) {
        put("languageFrom", languageFrom);
    }

    public List<String> getLanguagesTo() {
        return getOrDefault("languagesTo", languagesTo);
    }

    public void setLanguagesTo(List<String> languagesTo) {
        put("languagesTo", languagesTo);
    }

    public String getDelimiter() {
        return getOrDefault("delimiter", delimiter);
    }

    public void setDelimiter(String delimiter) {
        put("delimiter", delimiter);
    }

    public String getAdditionalInformationDelimiter() {
        return getOrDefault("additionalInformationDelimiter", additionalInformationDelimiter);
    }

    public void setAdditionalInformationDelimiter(String additionalInformationDelimiter) {
        put("additionalInformationDelimiter", additionalInformationDelimiter);
    }

    public String getTranslationDelimiter() {
        return getOrDefault("translationDelimiter", translationDelimiter);
    }

    public void setTranslationDelimiter(String translationDelimiter) {
        put("translationDelimiter", translationDelimiter);
    }

    public List<String> getArticles() {
        return getOrDefault("articles", articles);
    }

    public void setArticles(List<String> articles) {
        put("articles", articles);
    }

    public String getTopicFlag() {
        return getOrDefault("topicFlag", topicFlag);
    }

    public void setTopicFlag(String topicFlag) {
        put("topicFlag", topicFlag);
    }

    public String getTopicDelimiter() {
        return getOrDefault("topicDelimiter", topicDelimiter);
    }

    public void setTopicDelimiter(String topicDelimiter) {
        put("topicDelimiter", topicDelimiter);
    }

    public String getPath() {
        return getOrDefault("path", path);
    }

    public void setPath(String path) {
        put("path", path);
    }
    public String getRootTopic() {
        return getOrDefault("rootTopic", rootTopic);
    }

    public void setRootTopic(String rootTopic) {
        put("rootTopic", rootTopic);
    }
}
