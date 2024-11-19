package com.example;

import java.sql.*;
import java.util.*;

public class DatabaseHandler {

    // Method to execute a SELECT query and return the results as a list of maps
    public static List<Map<String, Object>> executeSelectQuery(String query) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    // Method to execute an INSERT/UPDATE/DELETE query
    public int executeUpdateQuery(String query, List<Object> params) {
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
