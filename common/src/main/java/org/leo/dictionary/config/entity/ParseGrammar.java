package org.leo.dictionary.config.entity;

public class ParseGrammar extends ConfigParent {
    private final String language = "";
    private final String path = "";
    private final String rootTopic = "";
    private final String topicFlag = "\t_TOPIC_ ";
    private final String hintFlag = "\t_HINT_ ";
    private final String delimiter = "\\|";
    private final String placeholder = "___";
    private final boolean hintAtSameLine = false;

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

    public String getHintFlag() {
        return getOrDefault("hintFlag", hintFlag);
    }

    public void setHintFlag(String hintFlag) {
        put("hintFlag", hintFlag);
    }

    public String getDelimiter() {
        return getOrDefault("delimiter", delimiter);
    }

    public void setDelimiter(String delimiter) {
        put("delimiter", delimiter);
    }

    public String getPath() {
        return getOrDefault("path", path);
    }

    public void setPath(String path) {
        put("path", path);
    }

    public String getPlaceholder() {
        return getOrDefault("placeholder", placeholder);
    }

    public void setPlaceholder(String placeholder) {
        put("placeholder", placeholder);
    }

    public String getRootTopic() {
        return getOrDefault("rootTopic", rootTopic);
    }

    public void setRootTopic(String rootTopic) {
        put("rootTopic", rootTopic);
    }

    public boolean getHintAtSameLine() {
        return getOrDefault("hintAtSameLine", hintAtSameLine);
    }

    public void setHintAtSameLine(boolean hintAtSameLine) {
        put("hintAtSameLine", hintAtSameLine);
    }
}
