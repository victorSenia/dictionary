package org.leo.dictionary.entity;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Word {
    private long id;
    private String language;
    private String word;
    private String additionalInformation;
    private String article;
    private double knowledge = 0.0;
    private byte[] pronunciation;
    private List<Translation> translations;
    private List<Topic> topics;
    private byte[] image;

    public static String formatWord(Word word) {
        if (word == null) {
            return "";
        }
        return word.getFullWord() +
                (word.getAdditionalInformation() != null ? "," + word.getAdditionalInformation() : "") + " - " +
                word.getFormattedTranslations(", ");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public double getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(double knowledge) {
        this.knowledge = knowledge;
    }

    public void increaseKnowledge(double increase) {
        this.knowledge += increase;
    }

    public byte[] getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(byte[] pronunciation) {
        this.pronunciation = pronunciation;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getFullWord() {
        return (getArticle() != null ? getArticle() : "") + getWord();
    }

    @Override
    public String toString() {
        return "Word{" +
                "language='" + language + '\'' +
                ", word='" + getFullWord() + '\'' +
                (additionalInformation != null ? ", additionalInformation='" + additionalInformation + '\'' : "") +
                '}';
    }

    public String getFormattedTranslations(String delimiter) {
        return getTranslations() != null ? getTranslations().stream().map(Translation::getTranslation).collect(Collectors.joining(delimiter)) : "";
    }

    public void updateWord(Word updatedWord, Set<String> languageTo) {
        word = updatedWord.word;
        additionalInformation = updatedWord.additionalInformation;
        language = updatedWord.language;
        article = updatedWord.article;
        translations = updatedWord.translations.stream().filter(translation -> languageTo == null || languageTo.isEmpty() || languageTo.contains(translation.getLanguage())).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return Objects.equals(language, word1.language) && Objects.equals(word, word1.word) && Objects.equals(additionalInformation, word1.additionalInformation) && Objects.equals(article, word1.article);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, word, additionalInformation, article);
    }
}
