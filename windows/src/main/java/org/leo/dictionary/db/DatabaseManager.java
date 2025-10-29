package org.leo.dictionary.db;

import org.leo.dictionary.entity.Topic;
import org.leo.dictionary.entity.Translation;
import org.leo.dictionary.entity.Word;
import org.leo.dictionary.helper.SerializeUtils;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DatabaseManager extends DatabaseManagerParent<QueryResult> {

    private final DatabaseHelper dbHelper;
    private final Connection connection;

    public DatabaseManager(DatabaseHelper dbHelper, Connection connection) {
        this.dbHelper = dbHelper;
        this.connection = connection;
        dbHelper.ensureInitialized(connection);
    }


    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int findColumn(QueryResult src, String name) {
        try {
            return src.rs().findColumn(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean moveToNext(QueryResult src) {
        try {
            return src.rs().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected long getLong(QueryResult src, int index) {
        try {
            return src.rs().getLong(index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected double getDouble(QueryResult src, int index) {
        try {
            return src.rs().getDouble(index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getString(QueryResult src, int index) {
        try {
            return src.rs().getString(index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getInt(QueryResult src, int index) {
        try {
            return src.rs().getInt(index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected byte[] getBlob(QueryResult src, int index) {
        try {
            return src.rs().getBytes(index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getFirstIndex() {
        return 1;
    }

    public synchronized <T> T executeInTransaction(Supplier<T> supplier) {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = supplier.get();
                connection.commit();
                return result;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        return this.connection;
    }

    protected long insertWord(Word word) {
        long id = getWordId(word);
        if (id != -1) return id;

        String sql = "INSERT INTO " + DatabaseConstants.TABLE_NAME_WORD +
                " (" + DatabaseConstants.COLUMN_LANGUAGE + ", " +
                DatabaseConstants.WORD_COLUMN_WORD + ", " +
                DatabaseConstants.WORD_COLUMN_ARTICLE + ", " +
                DatabaseConstants.WORD_COLUMN_ADDITIONAL_INFORMATION + ", " +
                DatabaseConstants.WORD_COLUMN_KNOWLEDGE + ") VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, word.getLanguage());
            stmt.setString(2, word.getWord());
            stmt.setString(3, word.getArticle());
            stmt.setString(4, word.getAdditionalInformation());
            stmt.setDouble(5, word.getKnowledge());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(getFirstIndex());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    public long insertTopic(Topic topic) {
        if (topic.getId() > 0) return topic.getId();
        long id = getTopicId(topic);
        if (id != -1) return id;

        String sql = "INSERT INTO " + DatabaseConstants.TABLE_NAME_TOPIC +
                " (" + DatabaseConstants.COLUMN_LANGUAGE + ", " +
                DatabaseConstants.TOPIC_COLUMN_LEVEL + ", " +
                DatabaseConstants.TOPIC_COLUMN_ROOT_ID + ", " +
                DatabaseConstants.TOPIC_COLUMN_NAME + ") VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, topic.getLanguage());
            stmt.setInt(2, topic.getLevel());

            if (topic.getRoot() != null) {
                stmt.setLong(3, insertTopic(topic.getRoot()));
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setString(4, topic.getName());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long insertedId = rs.getLong(getFirstIndex());
                    topic.setId(insertedId);
                    return insertedId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    public long insertTranslation(Translation translation, long wordId) {
        long id = getTranslationId(translation, wordId);
        if (id != -1) return id;

        String sql = "INSERT INTO " + DatabaseConstants.TABLE_NAME_TRANSLATION +
                " (" + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + ", " +
                DatabaseConstants.TRANSLATION_COLUMN_TRANSLATION + ", " +
                DatabaseConstants.COLUMN_LANGUAGE + ") VALUES (?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, wordId);
            stmt.setString(2, translation.getTranslation());
            stmt.setString(3, translation.getLanguage());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long insertedId = rs.getLong(getFirstIndex());
                    translation.setId(insertedId);
                    return insertedId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    public long insertWordTopicLink(long wordId, long topicId) {
        long id = getId(DatabaseConstants.TABLE_NAME_WORD_TOPIC,
                DatabaseConstants.COLUMN_ID + " = ? AND " + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " = ?",
                Long.toString(topicId), Long.toString(wordId));
        if (id != -1) return id;

        String sql = "INSERT INTO " + DatabaseConstants.TABLE_NAME_WORD_TOPIC +
                " (" + DatabaseConstants.COLUMN_ID + ", " + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + ") VALUES (?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, topicId);
            stmt.setLong(2, wordId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(getFirstIndex());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    public int deleteWordTopicLink(long wordId, long topicId) {
        String sql = "DELETE FROM " + DatabaseConstants.TABLE_NAME_WORD_TOPIC +
                " WHERE " + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " = ? AND " + DatabaseConstants.COLUMN_ID + " = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, wordId);
            stmt.setLong(2, topicId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected QueryResult rawQuery(String fullSql, String... selectionArgs) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(fullSql);
            if (selectionArgs != null) {
                int i = 1;
                for (String arg : selectionArgs) {
                    stmt.setString(i++, arg);
                }
            }
            return new QueryResult(stmt, stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected QueryResult query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit) {
        String sql = "SELECT " + (distinct ? "DISTINCT " : "")
                + String.join(", ", columns)
                + " FROM " + table
                + (selection != null ? " WHERE " + selection : "")
                + (orderBy != null ? " ORDER BY " + orderBy : "")
                + (limit != null ? " LIMIT " + limit : "");
        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            if (selectionArgs != null) {
                for (int i = 0; i < selectionArgs.length; i++) {
                    stmt.setString(i + 1, selectionArgs[i]);
                }
            }
            return new QueryResult(stmt, stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int deleteForLanguage(String language) {
        List<String> wordIds = getWordIdsForLanguage(language);
        for (int fromIndex = 0; fromIndex < wordIds.size(); fromIndex += DatabaseConstants.PAGE_SIZE) {
            deleteWords(wordIds.subList(fromIndex, Math.min(wordIds.size(), fromIndex + DatabaseConstants.PAGE_SIZE)));
        }

        String sql = "DELETE FROM " + DatabaseConstants.TABLE_NAME_TOPIC + " WHERE " + DatabaseConstants.COLUMN_LANGUAGE + " = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, language);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return wordIds.size();
    }

    @Override
    public void vacuum() {
        try (Statement stmt = getConnection().createStatement()) {
            dbHelper.execSQL(stmt, "VACUUM");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected int deleteWords(List<String> wordIds) {
        if (wordIds.isEmpty()) {
            return 0;
        }
        String placeholders = createPlaceholders(wordIds.size());

        String sqlWord = "DELETE FROM " + DatabaseConstants.TABLE_NAME_WORD + " WHERE " + DatabaseConstants.COLUMN_ID + " IN (" + placeholders + ")";
        String sqlTranslation = "DELETE FROM " + DatabaseConstants.TABLE_NAME_TRANSLATION + " WHERE " + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " IN (" + placeholders + ")";
        String sqlWordTopic = "DELETE FROM " + DatabaseConstants.TABLE_NAME_WORD_TOPIC + " WHERE " + DatabaseConstants.TRANSLATION_COLUMN_WORD_ID + " IN (" + placeholders + ")";

        for (String sql : new String[]{sqlWord, sqlTranslation, sqlWordTopic}) {
            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                for (int i = 0; i < wordIds.size(); i++) {
                    stmt.setString(i + 1, wordIds.get(i));
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return wordIds.size();
    }

    public int updateWord(Word word) {
        String sql = "UPDATE " + DatabaseConstants.TABLE_NAME_WORD + " SET " +
                DatabaseConstants.COLUMN_LANGUAGE + " = ?, " +
                DatabaseConstants.WORD_COLUMN_WORD + " = ?, " +
                DatabaseConstants.WORD_COLUMN_ADDITIONAL_INFORMATION + " = ?, " +
                DatabaseConstants.WORD_COLUMN_ARTICLE + " = ?, " +
                DatabaseConstants.WORD_COLUMN_KNOWLEDGE + " = ? WHERE " +
                DatabaseConstants.COLUMN_ID + " = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, word.getLanguage());
            stmt.setString(2, word.getWord());
            stmt.setString(3, word.getAdditionalInformation());
            stmt.setString(4, word.getArticle());
            stmt.setDouble(5, word.getKnowledge());
            stmt.setLong(6, word.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateTopic(Topic topic) {
        String sql = "UPDATE " + DatabaseConstants.TABLE_NAME_TOPIC + " SET " +
                DatabaseConstants.COLUMN_LANGUAGE + " = ?, " +
                DatabaseConstants.TOPIC_COLUMN_LEVEL + " = ?, " +
                DatabaseConstants.TOPIC_COLUMN_ROOT_ID + " = ?, " +
                DatabaseConstants.TOPIC_COLUMN_NAME + " = ? WHERE " +
                DatabaseConstants.COLUMN_ID + " = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, topic.getLanguage());
            stmt.setInt(2, topic.getLevel());

            if (topic.getRoot() != null) {
                stmt.setLong(3, insertTopic(topic.getRoot()));
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setString(4, topic.getName());
            stmt.setLong(5, topic.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateTranslation(Translation translation) {
        String sql = "UPDATE " + DatabaseConstants.TABLE_NAME_TRANSLATION + " SET " +
                DatabaseConstants.COLUMN_LANGUAGE + " = ?, " +
                DatabaseConstants.TRANSLATION_COLUMN_TRANSLATION + " = ? WHERE " +
                DatabaseConstants.COLUMN_ID + " = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, translation.getLanguage());
            stmt.setString(2, translation.getTranslation());
            stmt.setLong(3, translation.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTranslation(long id) {
        String sql = "DELETE FROM " + DatabaseConstants.TABLE_NAME_TRANSLATION + " WHERE " + DatabaseConstants.COLUMN_ID + " = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long insertConfigurationPreset(String name, Map<String, ?> data) {
        String sql = "INSERT INTO " + DatabaseConstants.TABLE_NAME_CONFIGURATION_PRESET +
                " (" + DatabaseConstants.COLUMN_ID + ", " + DatabaseConstants.CONFIGURATION_PRESET_DATA + ") VALUES (?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setBytes(2, SerializeUtils.serializeToBytes(data));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(getFirstIndex());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public int updateConfigurationPreset(String name, Map<String, ?> data) {
        String sql = "UPDATE " + DatabaseConstants.TABLE_NAME_CONFIGURATION_PRESET +
                " SET " + DatabaseConstants.CONFIGURATION_PRESET_DATA + " = ? WHERE " +
                DatabaseConstants.COLUMN_ID + " = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBytes(1, SerializeUtils.serializeToBytes(data));
            stmt.setString(2, name);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int deleteConfigurationPreset(String name) {
        String sql = "DELETE FROM " + DatabaseConstants.TABLE_NAME_CONFIGURATION_PRESET +
                " WHERE " + DatabaseConstants.COLUMN_ID + " = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

