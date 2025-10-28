package org.leo.dictionary;

import org.leo.dictionary.audio.AudioService;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.config.entity.Configuration;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.word.provider.WordProvider;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlayServiceImpl implements PlayService {
    private final static Logger LOGGER = Logger.getLogger(PlayServiceImpl.class.getName());
    private final Object playLock = new Object();
    private ConfigurationService configurationService;
    private AudioService audioService;
    private UiUpdater uiUpdater;

    private WordProvider wordProvider;
    private ListIterator<Word> wordsIterator;
    private List<Word> words;
    private PlayThread playThread;
    private Set<String> playTranslationFor;

    @Override
    public void play() {
        LOGGER.fine("play");
        playThread = new PlayThread();
        playThread.start();
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setAudioService(AudioService audioService) {
        this.audioService = audioService;
    }

    public void setUiUpdater(UiUpdater uiUpdater) {
        this.uiUpdater = uiUpdater;
    }

    public void setWordProvider(WordProvider wordProvider) {
        this.wordProvider = wordProvider;
    }

    private void stop() {
        if (isPlaying()) {
            updateUiState(null);
            LOGGER.fine("stop");
            interrupt();
            audioService.abort();
        }
    }

    @Override
    public void pause() {
        LOGGER.fine("pause");
        stop();
    }

    @Override
    public void setWords(List<Word> words) {
        this.words = words;
        LOGGER.info(this.words.toString());
        wordsIterator = this.words.listIterator();
    }

    @Override
    public void setPlayTranslationFor(Set<String> playTranslationFor) {
        this.playTranslationFor = playTranslationFor;
    }

    @Override
    public void next() {
        LOGGER.fine("next");
        stop();
        setNext();
        play();
    }

    @Override
    public void playFrom(int index) {
        LOGGER.fine("next");
        stop();
        createIteratorFrom(index);
        play();
    }

    @Override
    public boolean isPlaying() {
        return playThread != null && !playThread.isInterrupted();
    }

    private void createIteratorFrom(int index) {
        synchronized (playLock) {
            if (index < 0) {
                index = words.size() - 1;
            }
            if (index >= words.size() || words.isEmpty()) {
                wordsIterator = words.listIterator();
            } else {
                wordsIterator = words.listIterator(index);
            }
        }
    }

    @Override
    public void previous() {
        LOGGER.fine("previous");
        stop();
        setPrevious();
        play();
    }

    private void playWord() {
        if (words.isEmpty()) {
            return;
        }
        synchronized (playLock) {
            if (wordsIterator == null || !wordsIterator.hasNext()) {
                wordsIterator = words.listIterator();
            }
            try {
                while (wordsIterator.hasNext()) {
                    Word word = wordsIterator.next();
                    playWord(word);
                    if (!wordsIterator.hasNext()) {
                        wordsIterator = words.listIterator();
                    }
                }
            } catch (InterruptedException e) {
                setPrevious();
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException) {
                    setPrevious();
                } else {
                    throw e;
                }
            }
        }
    }

    private void interrupt() {
        if (playThread != null) {
            playThread.interrupt();
            playThread = null;
        }
    }

    private void setNext() {
        synchronized (playLock) {
            if (wordsIterator == null || !wordsIterator.hasNext()) {
                wordsIterator = words.listIterator();
            } else {
                wordsIterator.next();
            }
        }
    }

    private void setPrevious() {
        synchronized (playLock) {
            if (wordsIterator == null || !wordsIterator.hasPrevious()) {
                wordsIterator = words.listIterator(words.size() - 1);
            } else {
                wordsIterator.previous();
            }
        }
    }

    private void playWord(Word word) throws InterruptedException {
        updateUiState(word);
        delay(getConfiguration().getGeneral().getDelayBefore());
        playWordAudio(word);
        spell(word);
        playTranslation(word, 0);
        if (getConfiguration().getRepeat().getTimes() > 1) {
            for (int i = 1; i < getConfiguration().getRepeat().getTimes(); i++) {
                delay(getConfiguration().getRepeat().getDelay());
                playWordAudio(word);
                if (getConfiguration().getSpelling().isEachTime()) {
                    spell(word);
                }
                if (getConfiguration().getTranslation().isEachTime()) {
                    playTranslation(word, i);
                }
            }
        }
        updateWord(word);
    }

    private void playTranslation(Word word, int translation) throws InterruptedException {
        if (getConfiguration().getTranslation().isActive()) {
            if (getConfiguration().getTranslation().isAllTranslations()) {
                for (Translation t : getTranslations(word)) {
                    playTranslation(t);
                }
            } else {
                playTranslation(chooseTranslation(word, translation));
            }
        }
    }

    private Configuration getConfiguration() {
        return configurationService.getConfiguration();
    }

    private void updateWord(Word word) {
        word.increaseKnowledge(getConfiguration().getGeneral().getKnowledgeIncrease());
        wordProvider.updateWord(word);
    }

    private void playWordAudio(Word word) throws InterruptedException {
        String fullWord = word.getWord();
        if (getConfiguration().getGeneral().isIncludeArticle()) {
            fullWord = word.getFullWord();
        }
        playAudio(word.getLanguage(), fullWord);
        delay(getConfiguration().getGeneral().getDelayPerLetterAfter() * fullWord.length());
    }

    private void updateUiState(Word word) {
        if (uiUpdater != null) {
            uiUpdater.updateState(word, wordsIterator.previousIndex());
        }
    }

    private void playAudio(String language, String word) {
        audioService.play(language, word);
    }

    private Translation chooseTranslation(Word word, int translation) {
        //TODO choose random
        //TODO fix no translation present
        List<Translation> wordTranslations = getTranslations(word);
        if (translation < wordTranslations.size()) {
            return wordTranslations.get(translation);
        } else {
            return wordTranslations.get(translation % wordTranslations.size());
        }
    }

    private List<Translation> getTranslations(Word word) {
        List<Translation> wordTranslations = word.getTranslations();
        if (playTranslationFor != null && !playTranslationFor.isEmpty()) {
            wordTranslations = word.getTranslations().stream()
                    .filter(t -> playTranslationFor.contains(t.getLanguage()))
                    .collect(Collectors.toList());
        }
        return wordTranslations;
    }

    private void playTranslation(Translation translation) throws InterruptedException {
        delay(getConfiguration().getTranslation().getDelay());
        playAudio(translation.getLanguage(), translation.getTranslation());
    }

    private void spell(Word word) throws InterruptedException {
        if (getConfiguration().getSpelling().isActive()) {
            delay(getConfiguration().getSpelling().getDelay() - getConfiguration().getSpelling().getLetterDelay());//TODO skip first delay
            for (String letter : word.getWord().split("")) {
                delay(getConfiguration().getSpelling().getLetterDelay());
                playAudio(word.getLanguage(), letter.toUpperCase(Locale.forLanguageTag(word.getLanguage().substring(0, 2))));
            }
        }
    }

    private void delay(long delay) throws InterruptedException {
        if (delay > 0) {
            Thread.sleep(delay);
        }
    }

    private class PlayThread extends Thread {
        @Override
        public void run() {
            playWord();
        }
    }
}
