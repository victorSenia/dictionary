package org.leo.dictionary.entity;

import java.util.Objects;

public class Topic {

    private long id;
    private String language;
    private String name;
    private int level;
    private Topic root;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Topic getRoot() {
        return root;
    }

    public void setRoot(Topic root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return level == topic.level && Objects.equals(language, topic.language) && Objects.equals(name, topic.name) && Objects.equals(root, topic.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, name, level);
    }
}
