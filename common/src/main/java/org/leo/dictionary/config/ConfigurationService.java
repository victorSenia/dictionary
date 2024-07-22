package org.leo.dictionary.config;

import org.leo.dictionary.config.entity.Configuration;

public class ConfigurationService {
    private Configuration configuration;

    private ConfigurationReader configurationReader;

    public Configuration getConfiguration() {
        if (configuration == null) {
            fillConfiguration();
        }
        return configuration;
    }

    public void fillConfiguration() {
        configuration = configurationReader.fillConfiguration();
    }

    public void setConfigurationReader(ConfigurationReader configurationReader) {
        this.configurationReader = configurationReader;
    }
}
