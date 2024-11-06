package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

public class Database {
    // Connection details
    private static final String URL = "jdbc:mysql://localhost:3306/football_league_dbms";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    // Method to establish and return a connection to the database
    public static Connection getConnection() throws SQLException {
        System.out.println("Attempting to connect to the database...");
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

    // Method to authenticate user and return user details
    public static Optional<Map<String, String>> authenticateUser(String username, String hashedPassword) {
        String query = "SELECT username, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> userDetails = new HashMap<>();
                    userDetails.put("username", rs.getString("username"));
                    userDetails.put("role", rs.getString("role"));
                    return Optional.of(userDetails);
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // Method to get user role
    public static String getUserRole(String username) throws SQLException {
        String query = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        }
        return "user"; // Default role if not found
    }

    // Method to update last login time
    public static void updateLastLogin(String username) {
        String query = "UPDATE user_data SET last_login = CURRENT_TIMESTAMP WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to check if user exists
    public static boolean userExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to create new user
    public static boolean createUser(String username, String hashedPassword, String role) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role.toLowerCase());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Also create entry in user_data table
                String userDataQuery = "INSERT INTO user_data (username) VALUES (?)";
                try (PreparedStatement userDataStmt = conn.prepareStatement(userDataQuery)) {
                    userDataStmt.setString(1, username);
                    userDataStmt.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Method to get user data
    public static Map<String, Object> getUserData(String username) {
        String query = "SELECT * FROM user_data WHERE username = ?";
        Map<String, Object> userData = new HashMap<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userData.put("username", rs.getString("username"));
                    userData.put("last_login", rs.getTimestamp("last_login"));
                    // Add any other user data fields here
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user data: " + e.getMessage());
            e.printStackTrace();
        }

        return userData;
    }
}