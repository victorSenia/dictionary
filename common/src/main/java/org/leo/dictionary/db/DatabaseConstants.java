package org.leo.dictionary.db;

public interface DatabaseConstants<T> {
    String DATABASE_NAME = "dictionary.db";
    String TABLE_NAME_TOPIC = "topic";
    String TABLE_NAME_TRANSLATION = "translation";
    String TABLE_NAME_WORD = "word";
    String TABLE_NAME_WORD_TOPIC = "word_topic";
    String TABLE_NAME_CONFIGURATION_PRESET = "configuration_preset";
    String COLUMN_ID = "id";
    String COLUMN_LANGUAGE = "language";
    String WORD_COLUMN_WORD = "word";
    String WORD_COLUMN_ADDITIONAL_INFORMATION = "additional_information";
    String WORD_COLUMN_ARTICLE = "article";
    String WORD_COLUMN_KNOWLEDGE = "knowledge";
    String TOPIC_COLUMN_NAME = "name";
    String TOPIC_COLUMN_LEVEL = "level";
    String TOPIC_COLUMN_ROOT_ID = "root";
    String TRANSLATION_COLUMN_WORD_ID = "word_" + COLUMN_ID;
    String TRANSLATION_COLUMN_TRANSLATION = "translation";
    String CONFIGURATION_PRESET_DATA = "data";

    int DB_VERSION = 2;
    int PAGE_SIZE = 200;

    default void dropTables(T db) {
        execSQL(db, "DROP TABLE IF EXISTS " + TABLE_NAME_WORD_TOPIC);
        execSQL(db, "DROP TABLE IF EXISTS " + TABLE_NAME_TRANSLATION);
        execSQL(db, "DROP TABLE IF EXISTS " + TABLE_NAME_WORD);
        execSQL(db, "DROP TABLE IF EXISTS " + TABLE_NAME_TOPIC);
        execSQL(db, "DROP TABLE IF EXISTS " + TABLE_NAME_CONFIGURATION_PRESET);
    }

    void execSQL(T db, String sql);

    default void onUpgrade(T db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    default void onCreate(T db) {
        execSQL(db, "CREATE TABLE " + TABLE_NAME_TOPIC + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_LANGUAGE + " TEXT NOT NULL, " + TOPIC_COLUMN_NAME + " TEXT NOT NULL, " + TOPIC_COLUMN_LEVEL + " INTEGER, " + TOPIC_COLUMN_ROOT_ID + " INTEGER);");
        execSQL(db, "CREATE TABLE " + TABLE_NAME_WORD + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_LANGUAGE + " TEXT NOT NULL, " + WORD_COLUMN_WORD + " TEXT NOT NULL, " + WORD_COLUMN_ADDITIONAL_INFORMATION + " TEXT, " + WORD_COLUMN_ARTICLE + " TEXT, " + WORD_COLUMN_KNOWLEDGE + " REAL);");
        execSQL(db, "CREATE TABLE " + TABLE_NAME_TRANSLATION + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_LANGUAGE + " TEXT NOT NULL, " + TRANSLATION_COLUMN_TRANSLATION + " TEXT NOT NULL, " + TRANSLATION_COLUMN_WORD_ID + " INTEGER," + "CONSTRAINT fk_word FOREIGN KEY (" + TRANSLATION_COLUMN_WORD_ID + ") REFERENCES " + TABLE_NAME_WORD + "(" + COLUMN_ID + ")" + ");");
        execSQL(db, "CREATE TABLE " + TABLE_NAME_WORD_TOPIC + " (" + TRANSLATION_COLUMN_WORD_ID + " INTEGER, " + COLUMN_ID + " INTEGER, " + "CONSTRAINT fk_topic FOREIGN KEY (" + TRANSLATION_COLUMN_WORD_ID + ") REFERENCES " + TABLE_NAME_TRANSLATION + "(" + COLUMN_ID + ")," + "CONSTRAINT fk_word FOREIGN KEY (" + COLUMN_ID + ") REFERENCES " + TABLE_NAME_WORD + "(" + COLUMN_ID + ")" + ");");
        execSQL(db, "CREATE TABLE " + TABLE_NAME_CONFIGURATION_PRESET + " (" + COLUMN_ID + " TEXT  PRIMARY KEY, " + CONFIGURATION_PRESET_DATA + " BLOB" + ");");

        execSQL(db, "CREATE UNIQUE INDEX " + TABLE_NAME_TOPIC + "_unique1 " + " ON " + TABLE_NAME_TOPIC + " (" + COLUMN_LANGUAGE + ", " + TOPIC_COLUMN_LEVEL + ", " + TOPIC_COLUMN_ROOT_ID + ", " + TOPIC_COLUMN_NAME + ");");
        execSQL(db, "CREATE UNIQUE INDEX " + TABLE_NAME_WORD + "_unique1 " + " ON " + TABLE_NAME_WORD + " (" + COLUMN_LANGUAGE + ", " + WORD_COLUMN_WORD + ", " + WORD_COLUMN_ARTICLE + ", " + WORD_COLUMN_ADDITIONAL_INFORMATION + ");");
        execSQL(db, "CREATE UNIQUE INDEX " + TABLE_NAME_TRANSLATION + "_unique1 " + " ON " + TABLE_NAME_TRANSLATION + " (" + TRANSLATION_COLUMN_WORD_ID + ", " + COLUMN_LANGUAGE + ", " + TRANSLATION_COLUMN_TRANSLATION + ");");
        execSQL(db, "CREATE UNIQUE INDEX " + TABLE_NAME_WORD_TOPIC + "_unique1 " + " ON " + TABLE_NAME_WORD_TOPIC + " (" + COLUMN_ID + ", " + TRANSLATION_COLUMN_WORD_ID + ");");
    }
}
