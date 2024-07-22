package org.leo.dictionary.config.entity;

import java.util.Map;

public class Configuration extends ConfigParent {
    private final General general = new General();
    private final Translation translation = new Translation();
    private final Repeat repeat = new Repeat();
    private final Spelling spelling = new Spelling();

    public General getGeneral() {
        return general;
    }

    public Translation getTranslation() {
        return translation;
    }

    public Repeat getRepeat() {
        return repeat;
    }

    public Spelling getSpelling() {
        return spelling;
    }

    @Override
    public void setProperties(Map<Object, Object> properties) {
        super.setProperties(properties);
        general.setProperties(properties);
        translation.setProperties(properties);
        repeat.setProperties(properties);
        spelling.setProperties(properties);
    }
}
