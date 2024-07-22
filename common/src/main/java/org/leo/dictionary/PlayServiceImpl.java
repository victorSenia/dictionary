package org.leo.dictionary;

import org.leo.dictionary.audio.AudioService;
import org.leo.dictionary.config.ConfigurationService;
import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.word.provider.WordProvider;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PlayServiceImpl implements PlayService, ExternalWordProvider, ExternalVoiceService {
    private final static Logger LOGGER = Logger.getLogger(PlayServiceImpl.class.getName());
    private final Object playLock = new Object();
    private ConfigurationService configurationService;
    private AudioService audioService;
    private UiUpdater uiUpdater;

    private WordProvider wordProvider;
    private ListIterator<Word> wordsIterator;
    private List<Word> unknownWords;
    //    private List<Word> knownWords;//TODO not used
    private PlayThread playThread;
    private WordCriteria wordCriteria;

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

    public WordProvider getWordProvider() {
        return wordProvider;
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
    public void safeDelete(int index) {
        boolean playing = isPlaying();
        if (playing) {
            stop();
        }
        int previousIndex = wordsIterator.previousIndex();
        if (index <= previousIndex) {
            previousIndex--;
            uiUpdater.updateState(null, wordsIterator.previousIndex() - 1);
        }
        createIteratorFrom(index + 1);
        Word o = wordsIterator.next();
        wordsIterator.remove();
        if (playing) {
            playFrom(previousIndex + 1);
        } else {
            createIteratorFrom(previousIndex + 1);
        }
    }

    @Override
    public void safeUpdate(int index, Word updatedWord) {
        boolean playing = isPlaying();
        if (!playing) {
            unknownWords.get(index).updateWord(updatedWord, wordCriteria.getLanguageTo());
            return;
        }
        int previousIndex = wordsIterator.previousIndex();
        if (previousIndex + 2 < index || previousIndex > index) {//safe to update
            unknownWords.get(index).updateWord(updatedWord, wordCriteria.getLanguageTo());
            return;
        }
        stop();
        unknownWords.get(index).updateWord(updatedWord, wordCriteria.getLanguageTo());
        play();
    }

    @Override
    public void safeAdd(Word updatedWord) {
        boolean playing = isPlaying();
        if (playing) {
            stop();
        }
        int previousIndex = wordsIterator.previousIndex();
        createIteratorFrom(unknownWords.size() + 1);
        updatedWord.updateWord(updatedWord, wordCriteria.getLanguageTo());
        wordsIterator.add(updatedWord);
        if (playing) {
            playFrom(previousIndex + 1);
        } else {
            createIteratorFrom(previousIndex + 1);
        }
    }

    @Override
    public List<Word> findWords(WordCriteria criteria) {
        stop();
        this.wordCriteria = criteria;
        unknownWords = wordProvider.findWords(criteria);
        LOGGER.info(unknownWords.toString());
//        knownWords = wordProvider.findKnownWords();
        if (criteria.getShuffleRandom() != -1) {
            Collections.shuffle(unknownWords, new Random(criteria.getShuffleRandom()));
        }
        wordsIterator = unknownWords.listIterator();
        return unknownWords;
    }

    @Override
    public List<Topic> findTopics(String language, int level) {
        return wordProvider.findTopics(language, level);
    }

    @Override
    public List<Topic> findTopicsWithRoot(String language, String rootTopic, int upToLevel) {
        return wordProvider.findTopicsWithRoot(language, rootTopic, upToLevel);
    }

    @Override
    public List<String> languageFrom() {
        return wordProvider.languageFrom();
    }

    @Override
    public List<String> languageTo(String language) {
        return wordProvider.languageTo(language);
    }

    @Override
    public List<String> getVoicesNames(String language) {
        return audioService.getVoicesNames(language);
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
        setNext();
        play();
    }

    @Override
    public boolean isPlaying() {
        return playThread != null && !playThread.isInterrupted();
    }

    private void createIteratorFrom(int index) {
        synchronized (playLock) {
            if (index <= 0) {
                index = unknownWords.size();
            }
            if (index > unknownWords.size() || unknownWords.isEmpty()) {
                wordsIterator = unknownWords.listIterator();
            } else {
                wordsIterator = unknownWords.listIterator(index - 1);
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
        if (unknownWords.isEmpty()) {
            return;
        }
        synchronized (playLock) {
            if (wordsIterator == null || !wordsIterator.hasNext()) {
                wordsIterator = unknownWords.listIterator();
            }
            try {
                while (wordsIterator.hasNext()) {
                    Word word = wordsIterator.next();
                    playWord(word);
//                    if (word.getKnowledge() >= 1.) {
//                        wordsIterator.remove();
//                        knownWords.add(word);
//                    }
                    if (!wordsIterator.hasNext()) {
                        wordsIterator = unknownWords.listIterator();
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
                wordsIterator = unknownWords.listIterator();
            } else {
                wordsIterator.next();
            }
        }
    }

    private void setPrevious() {
        synchronized (playLock) {
            if (wordsIterator == null || !wordsIterator.hasPrevious()) {
                wordsIterator = unknownWords.listIterator(unknownWords.size() - 1);
            } else {
                wordsIterator.previous();
            }
        }
    }

    private void playWord(Word word) throws InterruptedException {
        updateUiState(word);
        delay(configurationService.getConfiguration().getGeneral().getDelayBefore());
        playWordAudio(word);
        spell(word);
        translation(chooseTranslation(word, 0));
        if (configurationService.getConfiguration().getRepeat().getTimes() > 1) {
            for (int i = 1; i < configurationService.getConfiguration().getRepeat().getTimes(); i++) {
                delay(configurationService.getConfiguration().getRepeat().getDelay());
                playWordAudio(word);
                if (configurationService.getConfiguration().getSpelling().isEachTime()) {
                    spell(word);
                }
                if (configurationService.getConfiguration().getTranslation().isEachTime()) {
                    translation(chooseTranslation(word, i));
                }
            }
        }
        updateWord(word);
    }

    private void updateWord(Word word) {
        word.increaseKnowledge(configurationService.getConfiguration().getGeneral().getKnowledgeIncrease());
        wordProvider.updateWord(word);
    }

    private void playWordAudio(Word word) throws InterruptedException {
        String fullWord = word.getWord();
        if (configurationService.getConfiguration().getGeneral().isIncludeArticle()) {
            fullWord = word.getFullWord();
        }
        playAudio(word.getLanguage(), fullWord);
        delay(configurationService.getConfiguration().getGeneral().getDelayPerLetterAfter() * fullWord.length());
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
        List<Translation> wordTranslations = word.getTranslations();
        Set<String> playTranslationFor = wordCriteria.getPlayTranslationFor();
        if (playTranslationFor != null && !playTranslationFor.isEmpty()) {
            wordTranslations = word.getTranslations().stream()
                    .filter(t -> playTranslationFor.contains(t.getLanguage()))
                    .collect(Collectors.toList());
        }
        if (translation < wordTranslations.size()) {
            return wordTranslations.get(translation);
        } else {
            return wordTranslations.get(translation % wordTranslations.size());
        }
    }

    private void translation(Translation translation) throws InterruptedException {
        if (configurationService.getConfiguration().getTranslation().isActive()) {
            delay(configurationService.getConfiguration().getTranslation().getDelay());
            playAudio(translation.getLanguage(), translation.getTranslation());
        }

    }

    private void spell(Word word) throws InterruptedException {
        if (configurationService.getConfiguration().getSpelling().isActive()) {
            delay(configurationService.getConfiguration().getSpelling().getDelay() - configurationService.getConfiguration().getSpelling().getLetterDelay());//TODO skip first delay
            for (String letter : word.getWord().split("")) {
                delay(configurationService.getConfiguration().getSpelling().getLetterDelay());
                playAudio(word.getLanguage(), letter.toUpperCase(Locale.forLanguageTag(word.getLanguage().substring(0, 2))));
            }
        }
    }

    private void delay(long delay) throws InterruptedException {
        if (delay > 0) {
            Thread.sleep(delay);
        }
    }

    public List<Word> getUnknownWords() {
        return unknownWords;
    }

    private class PlayThread extends Thread {
        @Override
        public void run() {
            playWord();
        }
    }
}
