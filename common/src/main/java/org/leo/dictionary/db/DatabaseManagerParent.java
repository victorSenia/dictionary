package org.leo.dictionary.db;

import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.entity.WordCriteria;
import org.leo.dictionary.helper.SerializeUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DatabaseManagerParent<T extends AutoCloseable> {

    public void mapTranslations(T src, Map<Long, Word> wordsMap) {
        int idIndex = findColumn(src, DatabaseConstants.COLUMN_ID);
        int languageIndex = findColumn(src, DatabaseConstants.COLUMN_LANGUAGE);
        int translationIndex = findColumn(src, DatabaseConstants.TRANSLATION_COLUMN_TRANSLATION);
        int wordIdIndex = findColumn(src, DatabaseConstants.TRANSLATION_COLUMN_WORD_ID);

        while (moveToNext(src)) {
            Translation translation = new Translation();
            translation.setId(getLong(src, idIndex));
            translation.setLanguage(getString(src, languageIndex));
            translation.setTranslation(getString(src, translationIndex));

            long wordId = getLong(src, wordIdIndex);
            Word word = wordsMap.get(wordId);

            if (word != null) {
                if (word.getTranslations() == null) {
                    word.setTranslations(new ArrayList<>());
                }
                word.getTranslations().add(translation);
            }
        }
    }

    public void mapWords(T src, List<Word> words) {
        int idIndex = findColumn(src, DatabaseConstants.COLUMN_ID);
        int languageIndex = findColumn(src, DatabaseConstants.COLUMN_LANGUAGE);
        int wordIndex = findColumn(src, DatabaseConstants.WORD_COLUMN_WORD);
        int articleIndex = findColumn(src, DatabaseConstants.WORD_COLUMN_ARTICLE);
        int additionalInformationIndex = findColumn(src, DatabaseConstants.WORD_COLUMN_ADDITIONAL_INFORMATION);
        int knowledgeIndex = findColumn(src, DatabaseConstants.WORD_COLUMN_KNOWLEDGE);

        while (moveToNext(src)) {
            Word word = new Word();
            word.setId(getLong(src, idIndex));
            word.setWord(getString(src, wordIndex));
            word.setArticle(getString(src, articleIndex));
            word.setAdditionalInformation(getString(src, additionalInformationIndex));
            word.setLanguage(getString(src, languageIndex));
            word.setKnowledge(getDouble(src, knowledgeIndex));

            words.add(word);
        }
    }

    public void mapTopics(T src, List<Topic> topics, Map<Long, Topic> loadedTopics) {
        int idIndex = findColumn(src, DatabaseConstants.COLUMN_ID);
        int nameIndex = findColumn(src, DatabaseConstants.TOPIC_COLUMN_NAME);
        int levelIndex = findColumn(src, DatabaseConstants.TOPIC_COLUMN_LEVEL);
        int rootIndex = findColumn(src, DatabaseConstants.TOPIC_COLUMN_ROOT_ID);
        int languageIndex = findColumn(src, DatabaseConstants.COLUMN_LANGUAGE);

        while (moveToNext(src)) {
            long id = getLong(src, idIndex);
            Topic topic = loadedTopics.get(id);

            if (topic != null) {
                topics.add(topic);
            } else {
                topic = new Topic();
                topic.setId(id);
                topic.setName(getString(src, nameIndex));
                topic.setLevel(getInt(src, levelIndex));
                topic.setLanguage(getString(src, languageIndex));

                long rootId = getLong(src, rootIndex);
                if (rootId > 0) {
                    Topic rootTopic = loadedTopics.get(rootId);
                    if (rootTopic != null) {
                        topic.setRoot(rootTopic);
                    }
                }

                topics.add(topic);
                loadedTopics.put(topic.getId(), topic);
            }
        }
    }

    protected abstract int findColumn(T src, String name);

    protected abstract boolean moveToNext(T src);

    protected abstract long getLong(T src, int index);

    protected abstract double getDouble(T src, int index);

    protected abstract String getString(T src, int index);

    protected abstract int getInt(T src, int index);

    protected abstract byte[] getBlob(T res, int dataIndex);


    public long insertFully(Word word) {
        long wordId = insertWord(word);
        if (word.getTranslations() != null) {
            for (Translation translation : word.getTranslations()) {
                insertTranslation(translation, wordId);
            }
        }
        if (word.getTopics() != null) {
            for (Topic topic : word.getTopics()) {
                insertWordTopicLink(wordId, insertTopic(topic));
            }
        }
        return wordId;
    }

    public abstract <R> R executeInTransaction(Supplier<R> supplier);

    protected abstract long insertWord(Word word);

    public abstract long insertTopic(Topic topic);

    protected long getTopicId(Topic topic) {
        List<String> arguments = new ArrayList<>();
        arguments.add(topic.getLanguage());
        arguments.add(topic.getName());
        arguments.add(Integer.toString(topic.getLevel()));
        return getId(DatabaseConstants.TABLE_NAME_TOPIC,
                DatabaseConstants.COLUMN_LANGUAGE + " = ? AND " + DatabaseConstants.TOPIC_COLUMN_NAME + " = ? AND " + DatabaseConstants.TOPIC_COLUMN_LEVEL + " = ?" +
                        andNullOrEquals(DatabaseConstants.TOPIC_COLUMN_ROOT_ID, () -> topic.getRoot() != null ? Long.toString(insertTopic(topic.getRoot())) : null, arguments),
                arguments.toArray(new String[0]));
    }

    private String andNullOrEquals(String column, Supplier<String> supplier, List<String> arguments) {
        return " AND " + nullOrEquals(column, supplier, arguments);
    }

    private String nullOrEquals(String column, Supplier<String> supplier, List<String> arguments) {
        String value = supplier.get();
        if (value == null) {
            return column + " IS NULL ";
        }
        arguments.add(value);
        return column + " = ? ";
    }

    protected long getTranslationId(Translation translation, long wordId) {
        return getId(DatabaseConstants.TABLE_NAME_TRANSLATION,
                DatabaseConstants.COLUMN_LANGUAGE + " = ? AND " + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " = ? AND " + DatabaseConstants.TRANSLATION_COLUMN_TRANSLATION + " = ?",
                translation.getLanguage(), Long.toString(wordId), translation.getTranslation());
    }

    protected long getWordId(Word word) {
        List<String> arguments = new ArrayList<>();
        arguments.add(word.getLanguage());
        arguments.add(word.getWord());
        return getId(DatabaseConstants.TABLE_NAME_WORD,
                DatabaseConstants.COLUMN_LANGUAGE + " = ? AND " + DatabaseConstants.WORD_COLUMN_WORD + " = ? " +
                        andNullOrEquals(DatabaseConstants.WORD_COLUMN_ARTICLE, word::getArticle, arguments) +
                        andNullOrEquals(DatabaseConstants.WORD_COLUMN_ADDITIONAL_INFORMATION, word::getAdditionalInformation, arguments),
                arguments.toArray(new String[0]));
    }

    protected long getId(String table, String selection, String... selectionArg) {
        try (T res = query(true, table,
                new String[]{DatabaseConstants.COLUMN_ID}, selection,
                selectionArg,
                null, null)) {
            if (moveToNext(res)) {
                return getLong(res, getFirstIndex());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public abstract long insertTranslation(Translation translation, long wordId);

    public abstract long insertWordTopicLink(long wordId, long topicId);

    public abstract int deleteWordTopicLink(long wordId, long topicId);

    protected T fetchWordsCursor(WordCriteria criteria, boolean countOnly) {
        String sql = "SELECT " + (countOnly ? "COUNT (DISTINCT w." + DatabaseConstants.COLUMN_ID + ")" : "DISTINCT w.*") + " FROM " + DatabaseConstants.TABLE_NAME_WORD + " w";
        List<String> selectionArgs = new ArrayList<>();
        String where = " WHERE 1=1";
        if ((criteria.getTopicsOr() != null && !criteria.getTopicsOr().isEmpty()) ||
                (criteria.getRootTopics() != null && !criteria.getRootTopics().isEmpty())) {
            Set<String> topicIds = getTopicIds(criteria.getLanguageFrom(), criteria.getRootTopics(), criteria.getTopicsOr());
            sql += " INNER JOIN " + DatabaseConstants.TABLE_NAME_WORD_TOPIC + " t ON w." + DatabaseConstants.COLUMN_ID + " = t." + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " AND t." + DatabaseConstants.COLUMN_ID + " IN (" + createPlaceholders(topicIds.size()) + ")";
            selectionArgs.addAll(topicIds);
        }
        if ((criteria.getLanguageTo() != null && !criteria.getLanguageTo().isEmpty())) {
            sql += " INNER JOIN " + DatabaseConstants.TABLE_NAME_TRANSLATION + " tr ON w." + DatabaseConstants.COLUMN_ID + " = tr." + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " AND tr." + DatabaseConstants.COLUMN_LANGUAGE + " IN (" + createPlaceholders(criteria.getLanguageTo().size()) + ")";
            selectionArgs.addAll(criteria.getLanguageTo());
        }
        if (criteria.getLanguageFrom() != null) {
            where += " AND w." + DatabaseConstants.COLUMN_LANGUAGE + " = ?";
            selectionArgs.add(criteria.getLanguageFrom());
        }
        if (criteria.getKnowledgeFrom() != null) {
            where += " AND w." + DatabaseConstants.WORD_COLUMN_KNOWLEDGE + " >= ?";
            selectionArgs.add(criteria.getKnowledgeFrom().toString());
        }
        if (criteria.getKnowledgeTo() != null) {
            where += " AND w." + DatabaseConstants.WORD_COLUMN_KNOWLEDGE + " <= ?";
            selectionArgs.add(criteria.getKnowledgeTo().toString());
        }
        WordCriteria.WordsOrderMode mode = criteria.getWordsOrderMode();
        if (mode == null) {
            mode = criteria.getShuffleRandom() != -1
                    ? WordCriteria.WordsOrderMode.SHUFFLE
                    : WordCriteria.WordsOrderMode.SORTED;
        }
        String orderBy;
        switch (mode) {
            case IMPORT_ORDER:
            case SHUFFLE:
                orderBy = "w." + DatabaseConstants.COLUMN_ID;
                break;
            case SORTED:
            default:
                orderBy = "w." + DatabaseConstants.COLUMN_LANGUAGE
                        + ", w." + DatabaseConstants.WORD_COLUMN_WORD + " COLLATE NOCASE";
                break;
        }
        String fullSql = sql + where + (countOnly ? "" : " ORDER BY " + orderBy);

        return rawQuery(fullSql, selectionArgs.toArray(new String[0]));
    }

    protected abstract T rawQuery(String fullSql, String... selectionArgs);

    protected abstract T query(boolean distinct, String table, String[] columns,
                               String selection, String[] selectionArgs,
                               String orderBy, String limit);

    protected T fetchTranslationsCursor(Set<String> languages, List<String> wordIds) {
        String selection = DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " IN (" + createPlaceholders(wordIds.size()) + ")";
        List<String> selectionArgs = new ArrayList<>(wordIds);
        if (languages != null && !languages.isEmpty()) {
            selection += " AND " + DatabaseConstants.COLUMN_LANGUAGE + " IN (" + createPlaceholders(languages.size()) + ")";
            selectionArgs.addAll(languages);
        }
        String[] columns = new String[]{DatabaseConstants.COLUMN_ID, DatabaseConstants.COLUMN_LANGUAGE, DatabaseConstants.TRANSLATION_COLUMN_WORD_ID, DatabaseConstants.TRANSLATION_COLUMN_TRANSLATION};

        return query(false, DatabaseConstants.TABLE_NAME_TRANSLATION,
                columns, selection,
                selectionArgs.toArray(selectionArgs.toArray(new String[0])),
                DatabaseConstants.TRANSLATION_COLUMN_WORD_ID, null);
    }

    protected Set<String> getTopicIds(String languageFrom, Set<Topic> rootTopic, Set<Topic> topicsOr) {
        if (topicsOr != null && !topicsOr.isEmpty()) {
            return topicsOr.stream().map(topic -> Long.toString(topic.getId())).collect(Collectors.toSet());
        }
        try (T res = fetchTopics(languageFrom, null, true, rootTopic.stream().map(topic -> Long.toString(topic.getId())).collect(Collectors.toSet()))) {
            Set<String> ids = new HashSet<>();
            while (moveToNext(res)) {
                ids.add(getString(res, getFirstIndex()));
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected T fetchTopics(String language, String level, boolean idOnly, Set<String> rootId) {
        String selection = " 1 = 1";
        List<String> selectionArgs = new ArrayList<>();
        if (language != null) {
            selection += " AND " + DatabaseConstants.COLUMN_LANGUAGE + "= ?";
            selectionArgs.add(language);
        }
        if (level != null) {
            selection += " AND " + DatabaseConstants.TOPIC_COLUMN_LEVEL + "= ?";
            selectionArgs.add(level);
        }
        if (rootId != null && !rootId.isEmpty()) {
            selection += " AND " + DatabaseConstants.TOPIC_COLUMN_ROOT_ID + " IN (" + createPlaceholders(rootId.size()) + ")";
            selectionArgs.addAll(rootId);
        }
        String[] columns;
        if (idOnly) {
            columns = new String[]{DatabaseConstants.COLUMN_ID};
        } else {
            columns = new String[]{DatabaseConstants.COLUMN_ID, DatabaseConstants.COLUMN_LANGUAGE, DatabaseConstants.TOPIC_COLUMN_NAME, DatabaseConstants.TOPIC_COLUMN_LEVEL, DatabaseConstants.TOPIC_COLUMN_ROOT_ID};
        }

        return query(false, DatabaseConstants.TABLE_NAME_TOPIC,
                columns, selection,
                selectionArgs.toArray(selectionArgs.toArray(new String[0])),
                null, null);
    }

    public List<Topic> getTopics(String language, Set<Long> rootIds, String level) {
        HashMap<Long, Topic> loadedTopics = new HashMap<>();
        if (rootIds != null) {
            loadedTopics.putAll(findRootTopics(language).stream().collect(Collectors.toMap(Topic::getId, Function.identity())));
        }
        try (T res = fetchTopics(language, level, false, rootIds != null ? rootIds.stream().map(Object::toString).collect(Collectors.toSet()) : null)) {
            List<Topic> topics = new ArrayList<>();
            mapTopics(res, topics, loadedTopics);
            return topics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Topic> findRootTopics(String language) {
        return getTopics(language, null, "1");
    }

    public List<Topic> getTopicsForWord(String wordId, String language, String level, Map<Long, Topic> loadedTopics) {
        try (T res = rawQuery("SELECT t.* FROM " + DatabaseConstants.TABLE_NAME_TOPIC + " t " +
                        " INNER JOIN " + DatabaseConstants.TABLE_NAME_WORD_TOPIC + " tw " +
                        " ON t." + DatabaseConstants.COLUMN_ID + " = tw." + DatabaseConstants.COLUMN_ID +
                        " AND tw." + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + "= ?" +
                        " AND t." + DatabaseConstants.COLUMN_LANGUAGE + "= ?" +
                        " AND t." + DatabaseConstants.TOPIC_COLUMN_LEVEL + "= ?"
                , wordId, language, level)) {
            List<Topic> topics = new ArrayList<>();
            mapTopics(res, topics, loadedTopics);
            return topics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> languageFrom() {
        try (T res = query(true, DatabaseConstants.TABLE_NAME_WORD,
                new String[]{DatabaseConstants.COLUMN_LANGUAGE}, null,
                null,
                null, null)) {
            List<String> languages = new ArrayList<>();
            int columnIndex = findColumn(res, DatabaseConstants.COLUMN_LANGUAGE);
            while (moveToNext(res)) {
                languages.add(getString(res, columnIndex));
            }
            return languages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> languageTo(String language) {
        try (T res = languageToCursor(language)) {
            List<String> languages = new ArrayList<>();
            int columnIndex = findColumn(res, DatabaseConstants.COLUMN_LANGUAGE);
            while (moveToNext(res)) {
                languages.add(getString(res, columnIndex));
            }
            return languages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private T languageToCursor(String language) {
        if (language != null) {
            return rawQuery("SELECT DISTINCT t." + DatabaseConstants.COLUMN_LANGUAGE +
                    " FROM " + DatabaseConstants.TABLE_NAME_TRANSLATION + " t" + " INNER JOIN " + DatabaseConstants.TABLE_NAME_WORD + " w " +
                    "ON w." + DatabaseConstants.COLUMN_ID + " = t." + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID +
                    " AND w." + DatabaseConstants.COLUMN_LANGUAGE + "= ?", language);
        } else {
            return query(true, DatabaseConstants.TABLE_NAME_TRANSLATION,
                    new String[]{DatabaseConstants.COLUMN_LANGUAGE}, null,
                    null,
                    null, null);
        }
    }

    public List<Word> getWords(WordCriteria criteria) {
        return getWords(() -> fetchWordsCursor(criteria, false), criteria.getLanguageTo(), false);
    }

    public int countWords(WordCriteria criteria) {
        try (T res = fetchWordsCursor(criteria, true)) {
            if (moveToNext(res)) {
                return getInt(res, getFirstIndex());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    protected int getFirstIndex() {
        return 0;
    }

    public List<Word> getWordsForLanguage(String language, Set<Topic> rootTopics) {
        WordCriteria criteria = new WordCriteria();
        criteria.setLanguageFrom(language);
        criteria.setRootTopics(rootTopics);
        return getWords(() -> fetchWordsCursor(criteria, false), null, true);
    }


    private List<Word> getWords(Supplier<T> supplier, Set<String> languages, boolean includeTopics) {
        List<Word> words = new ArrayList<>();
        try (T res = supplier.get()) {
            mapWords(res, words);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<Long, Word> wordsMap = words.stream().collect(Collectors.toMap(Word::getId, Function.identity()));
        List<String> wordIds = words.stream().map(w -> Long.toString(w.getId())).collect(Collectors.toList());
        for (int fromIndex = 0; fromIndex < words.size(); fromIndex += DatabaseConstants.PAGE_SIZE) {
            try (T res = fetchTranslationsCursor(languages, wordIds.subList(fromIndex, Math.min(wordIds.size(), fromIndex + DatabaseConstants.PAGE_SIZE)))) {
                mapTranslations(res, wordsMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        words = words.stream().filter(this::hasTranslations).collect(Collectors.toList());
        if (includeTopics && !words.isEmpty()) {
            Map<Long, Topic> topics = new HashMap<>(getTopics(null, null, "1").stream().collect(Collectors.toMap(Topic::getId, Function.identity())));
            for (Word word : words) {
                word.setTopics(getTopicsForWord(String.valueOf(word.getId()), word.getLanguage(), "2", topics));//TODO
            }
        }
        return words;
    }

    private boolean hasTranslations(Word w) {
        return w.getTranslations() != null && !w.getTranslations().isEmpty();
    }

    protected String createPlaceholders(int length) {
        return String.join(", ", Collections.nCopies(length, "?"));
    }

    public int deleteWord(long id) {
        return deleteWords(Collections.singletonList(Long.toString(id)));
    }

    public abstract int deleteForLanguage(String language);

    public abstract void vacuum();

    protected abstract int deleteWords(List<String> wordIds);

    protected List<String> getWordIdsForLanguage(String language) {
        try (T res = query(true, DatabaseConstants.TABLE_NAME_WORD,
                new String[]{DatabaseConstants.COLUMN_ID}, DatabaseConstants.COLUMN_LANGUAGE + "= ?",
                new String[]{language},
                null, null)) {
            List<String> result = new ArrayList<>();
            int columnIndex = findColumn(res, DatabaseConstants.COLUMN_ID);
            while (moveToNext(res)) {
                result.add(getString(res, columnIndex));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Word findWord(long id) {
        List<Word> words = getWords(() -> getCursorForWordById(id), null, true);
        if (!words.isEmpty()) {
            return words.get(0);
        }
        return null;
    }

    private T getCursorForWordById(long id) {
        return query(true, DatabaseConstants.TABLE_NAME_WORD,
                new String[]{DatabaseConstants.COLUMN_ID, DatabaseConstants.COLUMN_LANGUAGE,
                        DatabaseConstants.WORD_COLUMN_WORD, DatabaseConstants.WORD_COLUMN_ADDITIONAL_INFORMATION,
                        DatabaseConstants.WORD_COLUMN_ARTICLE, DatabaseConstants.WORD_COLUMN_KNOWLEDGE},
                DatabaseConstants.COLUMN_ID + "= ?",
                new String[]{Long.toString(id)},
                null, null);

    }

    public abstract int updateWord(Word word);

    public abstract int updateTopic(Topic topic);

    public abstract int updateTranslation(Translation translation);

    public abstract void deleteTranslation(long id);

    public abstract long insertConfigurationPreset(String name, Map<String, ?> data);

    public abstract int updateConfigurationPreset(String name, Map<String, ?> data);

    public abstract int deleteConfigurationPreset(String name);

    public Map<String, ?> getConfigurationPreset(String name) {
        try (T res = query(true, DatabaseConstants.TABLE_NAME_CONFIGURATION_PRESET,
                new String[]{DatabaseConstants.CONFIGURATION_PRESET_DATA},
                DatabaseConstants.COLUMN_ID + "= ?",
                new String[]{name},
                null, null)) {
            int dataIndex = findColumn(res, DatabaseConstants.CONFIGURATION_PRESET_DATA);
            if (moveToNext(res)) {
                return (Map<String, ?>) SerializeUtils.deserializeBytes(getBlob(res, dataIndex));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<String> getConfigurationPresetNames() {
        try (T res = query(true, DatabaseConstants.TABLE_NAME_CONFIGURATION_PRESET,
                new String[]{DatabaseConstants.COLUMN_ID, DatabaseConstants.CONFIGURATION_PRESET_DATA},
                null, null, null, null)) {
            List<String> result = new ArrayList<>();
            int idIndex = findColumn(res, DatabaseConstants.COLUMN_ID);
            while (moveToNext(res)) {
                result.add(getString(res, idIndex));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}