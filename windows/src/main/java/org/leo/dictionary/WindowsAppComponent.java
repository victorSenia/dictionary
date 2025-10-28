package org.leo.dictionary;

import dagger.Component;
import javafx.util.Callback;
import org.leo.dictionary.audio.AudioService;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.FileConfigurationReader;
import org.leo.dictionary.word.provider.WordProvider;

import javax.inject.Singleton;

@Singleton
@Component(modules = {WindowsModule.class, ControllersModule.class})
interface WindowsAppComponent {

    PlayService playService();

    AudioService audioService();

    WordProvider externalWordProvider();

    ConfigurationService configurationService();

    FileConfigurationReader configurationReader();
    Callback<Class<?>, Object> controllerFactory();
//
//    AudioService externalVoiceService();
//
//    UiUpdater uiUpdater();
//
//
//    GrammarProvider externalGrammarProvider();
//
//    SentenceProvider externalSentenceProvider();
}
