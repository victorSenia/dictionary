package org.leo.dictionary.entity;

import java.util.List;
import java.util.Objects;

public class Sentence {
    private long id;
    private String sentence;
    private String language;
    private Topic topic;
    private List<Part> parts;
    private List<Translation> translations;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    public static class Part {

        protected final int partNumber;
        protected final String part;

        public Part(int partNumber, String part) {
            this.partNumber = partNumber;
            this.part = part;
        }

        public String getPart() {
            return part;
        }

        public void join(StringBuilder builder) {
            if (builder.length() != 0) {
                builder.append(' ');
            }
            builder.append(part);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Part part = (Part) o;
            return partNumber == part.partNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(partNumber);
        }
    }

    public static class Punctuation extends Part {
        public Punctuation(int partNumber, String part) {
            super(partNumber, part);
        }

        @Override
        public void join(StringBuilder builder) {
            builder.append(part);
        }
    }
}
