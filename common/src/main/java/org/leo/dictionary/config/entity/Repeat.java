package org.leo.dictionary.config.entity;

public class Repeat extends ConfigParent {
    private final int times = 2;
    private final long delay = 300L;

    public int getTimes() {
        return getOrDefault("times", times);
    }

    public long getDelay() {
        return getOrDefault("delay", delay);
    }
}
