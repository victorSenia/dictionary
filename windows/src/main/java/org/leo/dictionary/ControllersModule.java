package org.leo.dictionary;

import dagger.Module;
import dagger.Provides;
import javafx.util.Callback;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.FileConfigurationReader;
import org.leo.dictionary.controller.*;
import org.leo.dictionary.word.provider.WordProvider;

import javax.inject.Provider;

@Module
public class ControllersModule {

    @Provides
    public Callback<Class<?>, Object> provideControllerFactory(
            PlayService playService, WordProvider wordProvider,
            ConfigurationService configurationService, FileConfigurationReader configurationReader,
            Provider<MenuBarController> menuBarControllerProvider) {
        return (cls) -> {
            if (cls == DictionaryController.class)
                return new DictionaryController(playService, wordProvider);
            if (cls == PlaybackControlsFragmentController.class)
                return new PlaybackControlsFragmentController(playService);
            if (cls == WordsListFragmentController.class)
                return new WordsListFragmentController(playService);
            if (cls == ConfigWindowController.class)
                return new ConfigWindowController(configurationService, configurationReader);
            if (cls == TopicsListFragmentController.class)
                return new TopicsListFragmentController(wordProvider);
            if (cls == MenuBarController.class)
                return menuBarControllerProvider.get();


            // add other controllers
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Provides
    MenuBarController provideMenuBarController(Callback<Class<?>, Object> controllerFactory) {
        return new MenuBarController(controllerFactory);
    }
}
