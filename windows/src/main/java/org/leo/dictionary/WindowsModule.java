package org.leo.dictionary;

import dagger.Module;
import dagger.Provides;
import org.leo.dictionary.audio.AudioService;
import org.leo.dictionary.audio.WindowsAudioService;
import org.leo.dictionary.config.ConfigParser;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.FileConfigurationReader;
import org.leo.dictionary.config.entity.ParseWords;
import org.leo.dictionary.db.DatabaseHelper;
import org.leo.dictionary.db.DatabaseManager;
import org.leo.dictionary.word.provider.DBWordProvider;
import org.leo.dictionary.word.provider.FileWordProvider;
import org.leo.dictionary.word.provider.WordProvider;
import org.leo.dictionary.word.provider.WordProviderDelegate;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.leo.dictionary.db.DatabaseHelper.DB_URL;

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
    public ConfigurationService provideConfigurationService(FileConfigurationReader configurationReader) {
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setConfigurationReader(configurationReader);
        return configurationService;
    }

    @Provides
    @Singleton
    public FileConfigurationReader provideConfigurationReader() {
        FileConfigurationReader fileConfigurationReader = new FileConfigurationReader();
        fileConfigurationReader.setPath("app.properties");
        return fileConfigurationReader;
    }

    @Provides
    @Singleton
    public WordProvider provideWordProvider(FileWordProvider fileWordProvider, @Named("dbWordProvider") DBWordProvider dbWordProvider) {
        WordProviderDelegate wordProvider = new WordProviderDelegate();
        wordProvider.setWordProvider(dbWordProvider);
//        wordProvider.setWordProvider(fileWordProvider);
        return wordProvider;
    }

    @Provides
    @Singleton
    public FileWordProvider provideFileWordProvider(ParseWords configuration) {
        return createFileWordProvider(configuration);
    }

    public static FileWordProvider createFileWordProvider(ParseWords configuration) {
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

    public static ParseWords getDefaultParseWordsConfig() {
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
    public PlayService providePlayService(ConfigurationService configurationService, AudioService audioService, WordProvider wordProvider) {
        PlayServiceImpl playService = new PlayServiceImpl();
        playService.setConfigurationService(configurationService);
        playService.setAudioService(audioService);
        playService.setWordProvider(wordProvider);
        return playService;
    }

    @Provides
    @Singleton
    public DatabaseHelper provideDatabaseHelper() {
        return new DatabaseHelper();
    }

    @Provides
    @Singleton
    public Connection provideConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public DatabaseManager provideDatabaseManager(DatabaseHelper databaseHelper, Connection connection) {
        return new DatabaseManager(databaseHelper, connection);
    }

    @Provides
    @Singleton
    @Named("dbWordProvider")
    public DBWordProvider provideDBWordProvider(DatabaseManager databaseManager) {
        DBWordProvider dbWordProvider = new DBWordProvider();
        dbWordProvider.setDbManager(databaseManager);
        return dbWordProvider;
    }

}
