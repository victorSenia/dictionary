package org.leo.dictionary.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record QueryResult(PreparedStatement stmt, ResultSet rs) implements AutoCloseable {

    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
