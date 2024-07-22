package org.leo.dictionary.config.entity;

public class General extends ConfigParent {
    private final double knowledgeIncrease = 0.02;
    private final long delayBefore = 200L;
    private final long delayPerLetterAfter = 40L;
    private final boolean includeKnown = false;
    private final boolean includeArticle = true;

    public double getKnowledgeIncrease() {
        return getOrDefault("knowledgeIncrease", knowledgeIncrease);
    }

    public long getDelayBefore() {
        return getOrDefault("delayBefore", delayBefore);
    }

    public long getDelayPerLetterAfter() {
        return getOrDefault("delayPerLetterAfter", delayPerLetterAfter);
    }

    public boolean isIncludeKnown() {
        return getOrDefault("includeKnown", includeKnown);
    }

    public boolean isIncludeArticle() {
        return getOrDefault("includeArticle", includeArticle);
    }
}
