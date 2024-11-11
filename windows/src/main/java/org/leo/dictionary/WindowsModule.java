package org.leo.dictionary;

import dagger.Module;
import dagger.Provides;
import org.leo.dictionary.audio.AudioService;
import org.leo.dictionary.audio.WindowsAudioService;
import org.leo.dictionary.config.ConfigParser;
import org.leo.dictionary.config.ConfigurationReader;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.FileConfigurationReader;
import org.leo.dictionary.config.entity.ParseWords;
import org.leo.dictionary.word.provider.FileWordProvider;
import org.leo.dictionary.word.provider.WordProvider;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Module
public class WindowsModule {
    @Provides
    @Singleton
    public AudioService provideAudioService() {
        WindowsAudioService windowsAudioService = new WindowsAudioService();
        try {
            windowsAudioService.setup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return windowsAudioService;
    }

    @Provides
    @Singleton
    public ConfigurationService provideConfigurationService(ConfigurationReader configurationReader) {
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(configurationReader);
        return configurationService;
    }

    @Provides
    @Singleton
    public ConfigurationReader provideConfigurationReader() {
        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath("app.properties");
        return fileConfigurationReader;
    }

    @Provides
    @Singleton
    public WordProvider provideWordProvider(ParseWords configuration) {
        FileWordProvider fileWordProvider = new FileWordProvider();
        fileWordProvider.setConfiguration(configuration);
        return fileWordProvider;
    }

    @Provides
    @Singleton
    public ParseWords provideParseWordsConfiguration() {
        String pathName = "parseWordsConfig.properties";
        ParseWords parseWords = getDefaultParseWordsConfig();
        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        if (fileConfigurationReader.isExists(pathName)) {
            Map<?, ?> properties = fileConfigurationReader.readConfig(pathName);
            return ConfigParser.createConfig(properties, parseWords);
        } else {
            Properties properties = new Properties();
            ConfigParser.writeConfigInProperties(properties, parseWords);
            fileConfigurationReader.writeConfig(properties, pathName, null);
            return parseWords;
        }
    }

    public ParseWords getDefaultParseWordsConfig() {
        ParseWords config = new ParseWords();
        config.setProperties(new Properties());
        config.setDelimiter("\\|");
        config.setTranslationDelimiter(";");
        config.setTopicDelimiter("");
        config.setTopicFlag("\t");
        config.setAdditionalInformationDelimiter(",");
        config.setArticles(Arrays.asList("die ", "das ", "der "));
        config.setLanguageFrom("de_DE");
        config.setLanguagesTo(List.of("en_GB"));
        config.setPath("src/main/resources/test1.txt");
        return config;
    }

    @Provides
    @Singleton
    public WindowsApp provideWindowsApp(PlayServiceImpl playService, WordProvider wordProvider) {
        WindowsApp windowsApp = new WindowsApp();
        windowsApp.setPlayService(playService);
        windowsApp.setExternalWordProvider(wordProvider);
        return windowsApp;
    }

    @Provides
    @Singleton
    public PlayServiceImpl providePlayService(ConfigurationService configurationService, AudioService audioService, WordProvider wordProvider) {
        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordProvider(wordProvider);
        return playService;
    }
}
