package org.leo.dictionary.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.leo.dictionary.config.entity.Configuration;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void getConfigurationReadsValuesFromRealPropertiesFile() throws IOException {
        Path configPath = tempDir.resolve("app.properties");
        writeProperties(configPath, 111L, 5);

        FileConfigurationReader reader = new FileConfigurationReader();
        reader.setPath(configPath.toString());
        ConfigurationService service = new ConfigurationService();
        service.setConfigurationReader(reader);

        Configuration configuration = service.getConfiguration();

        assertNotNull(configuration);
        assertEquals(111L, configuration.getGeneral().getDelayBefore());
        assertEquals(5, configuration.getRepeat().getTimes());
    }

    @Test
    void getConfigurationCachesUntilFillConfigurationIsCalled() throws IOException {
        Path configPath = tempDir.resolve("cache.properties");
        writeProperties(configPath, 200L, 2);

        FileConfigurationReader reader = new FileConfigurationReader();
        reader.setPath(configPath.toString());
        ConfigurationService service = new ConfigurationService();
        service.setConfigurationReader(reader);

        Configuration first = service.getConfiguration();
        assertEquals(200L, first.getGeneral().getDelayBefore());
        assertEquals(2, first.getRepeat().getTimes());

        writeProperties(configPath, 450L, 7);

        Configuration cached = service.getConfiguration();
        assertEquals(200L, cached.getGeneral().getDelayBefore());
        assertEquals(2, cached.getRepeat().getTimes());

        service.fillConfiguration();
        Configuration reloaded = service.getConfiguration();
        assertEquals(450L, reloaded.getGeneral().getDelayBefore());
        assertEquals(7, reloaded.getRepeat().getTimes());
    }

    private static void writeProperties(Path path, long delayBefore, int repeatTimes) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("org.leo.dictionary.config.entity.General.delayBefore", String.valueOf(delayBefore));
        properties.setProperty("org.leo.dictionary.config.entity.Repeat.times", String.valueOf(repeatTimes));
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, "test");
        }
    }
}
