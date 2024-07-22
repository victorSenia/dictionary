package org.leo.dictionary.config.entity;

public class Spelling extends ConfigParent {
    private final boolean active = false;
    private final boolean eachTime = false;
    private final long delay = 100L;
    private final long letterDelay = 200L;

    public boolean isActive() {
        return getOrDefault("active", active);
    }

    public boolean isEachTime() {
        return getOrDefault("eachTime", eachTime);
    }

    public long getDelay() {
        return getOrDefault("delay", delay);
    }

    public long getLetterDelay() {
        return getOrDefault("letterDelay", letterDelay);
    }

}
