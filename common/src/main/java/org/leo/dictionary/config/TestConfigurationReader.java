package org.leo.dictionary.config;

import org.leo.dictionary.config.entity.Configuration;

public class TestConfigurationReader implements ConfigurationReader {

    public Configuration fillConfiguration() {
        return new Configuration();
    }
}
