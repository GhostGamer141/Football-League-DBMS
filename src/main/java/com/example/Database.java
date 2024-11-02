package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/football_league_dbms";
    private static final String USER = "root";
    private static final String PASSWORD = "C3a#palmavenuegm";

    public static Connection getConnection() throws SQLException {
        System.out.println("attempting connection...");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Method to test the connection
    public static void testConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Connection successful!");
            } else {
                System.out.println("Connection failed.");
            }
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
