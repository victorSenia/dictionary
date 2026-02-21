package org.leo.dictionary.config;

import org.leo.dictionary.config.entity.Configuration;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class FileConfigurationReader implements ConfigurationReader {
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    public Configuration fillConfiguration() {
        Configuration configuration = new Configuration();
        if (isExists(path)) {
            Map<Object, Object> properties = readConfig(path);
            configuration.setProperties(properties);
        } else {
            configuration.setProperties(new Properties());
        }
//            updateConfiguration(configuration);        //TODO
        return configuration;
    }

    public void updateConfiguration(Configuration configuration) {
        Properties properties = new Properties();
        ConfigParser.writeConfigInProperties(properties, configuration.getGeneral());
        ConfigParser.writeConfigInProperties(properties, configuration.getRepeat());
        ConfigParser.writeConfigInProperties(properties, configuration.getTranslation());
        ConfigParser.writeConfigInProperties(properties, configuration.getSpelling());
        writeConfig(properties, path, null);
    }

    public void writeConfig(Properties properties, String path, String comment) {
        new java.io.File(path).getAbsoluteFile().getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            properties.store(writer, comment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Object, Object> readConfig(String path) {
        Properties properties = new Properties();
        if (isExists(path)) {
            try (BufferedReader reader = getBufferedReader(path)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    public boolean isExists(String path) {
        return new java.io.File(path).exists();
    }

    protected BufferedReader getBufferedReader(String path) throws IOException {
        return new BufferedReader(new FileReader(path));
    }

}
