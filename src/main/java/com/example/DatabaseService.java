package com.example;

import java.sql.*;
import java.util.*;

public class DatabaseService {

    /**
     * Executes a SELECT query and returns results as a List of Maps
     * @param query The SQL query to execute
     * @param params Optional parameters for prepared statement
     * @return List of rows, each row is a Map of column name to value
     */
    public static List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = prepareStatement(conn, query, params);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database query failed: " + e.getMessage(), e);
        }

        return results;
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE query
     * @param query The SQL query to execute
     * @param params Optional parameters for prepared statement
     * @return Number of rows affected
     */
    public static int executeUpdate(String query, Object... params) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = prepareStatement(conn, query, params)) {

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database update failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a query that returns a single value
     * @param query The SQL query to execute
     * @param params Optional parameters for prepared statement
     * @return The single result value, or null if no result
     */
    public static Object executeScalar(String query, Object... params) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = prepareStatement(conn, query, params);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Database query failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a batch of updates
     * @param query The SQL query to execute
     * @param batchParams List of parameter arrays for batch execution
     * @return Array of update counts
     */
    public static int[] executeBatch(String query, List<Object[]> batchParams) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (Object[] params : batchParams) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.addBatch();
            }

            return stmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Database batch update failed: " + e.getMessage(), e);
        }
    }

    private static PreparedStatement prepareStatement(Connection conn, String query, Object... params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
}