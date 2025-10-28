package org.leo.dictionary.db;

import java.sql.*;

public class DatabaseHelper implements DatabaseConstants<Statement> {
    public static String DB_URL = "jdbc:sqlite:" + DATABASE_NAME;

    public static void main(String[] args) {
        DatabaseHelper helper = new DatabaseHelper();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            helper.ensureInitialized(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void ensureInitialized(Connection conn) throws SQLException {
        if (conn != null) {
            int oldVersion = getDatabaseVersion(conn);
            if (oldVersion != DB_VERSION) {
                try (Statement statement = conn.createStatement()) {
                    DatabaseConstants.super.onUpgrade(statement, oldVersion, DB_VERSION);
                }
                setDatabaseVersion(conn);
            }
        }
    }

    private int getDatabaseVersion(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA user_version;")) {
            return rs.getInt(1);
        }
    }

    private void setDatabaseVersion(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA user_version = " + DatabaseConstants.DB_VERSION + ";");
        }
    }

    @Override
    public void execSQL(Statement statement, String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
