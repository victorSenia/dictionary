package org.leo.dictionary.config.entity;

public class Translation extends ConfigParent {
    private final boolean active = true;
    private final long delay = 500L;
    private final boolean eachTime = true;

    public boolean isActive() {
        return getOrDefault("active", active);
    }

    public long getDelay() {
        return getOrDefault("delay", delay);
    }

    public boolean isEachTime() {
        return getOrDefault("eachTime", eachTime);
    }
}
