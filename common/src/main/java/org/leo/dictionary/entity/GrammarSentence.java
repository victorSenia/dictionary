package org.leo.dictionary.entity;

public class GrammarSentence {
    private long id;
    private String sentencePrefix;
    private String sentenceSuffix;
    private String language;
    private String answer;
    private Hint hint;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSentencePrefix() {
        return sentencePrefix;
    }

    public void setSentencePrefix(String sentencePrefix) {
        this.sentencePrefix = sentencePrefix;
    }

    public String getSentenceSuffix() {
        return sentenceSuffix;
    }

    public void setSentenceSuffix(String sentenceSuffix) {
        this.sentenceSuffix = sentenceSuffix;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Hint getHint() {
        return hint;
    }

    public void setHint(Hint hint) {
        this.hint = hint;
    }
}
