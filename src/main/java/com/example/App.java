package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Test database connection on startup
        Database.testConnection();

        // Initialize Javalin app
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7000);

        // Handle POST request to receive login data
        app.post("/login.html", ctx -> handleLogin(ctx));

        // Add API endpoint for league table data
        app.get("/api/league-table", ctx -> {
            List<Map<String, Object>> leagueTable = Database.getLeagueTable();
            System.out.println(leagueTable);

            ctx.json(leagueTable);
        });
    }

    // Function to handle the login process
    private static void handleLogin(Context ctx) {
        Map<String, String> userCredentials = getUserInfo(ctx);

        if (userCredentials != null) {
            String username = userCredentials.get("username");
            String password = userCredentials.get("password");
            System.out.println("Received login details: " + username + " " + password);

            // Validate user credentials and retrieve role
            String role = validateUserCredentials(username, password);
            if (role != null) {
                // Successful login - redirect to dashboard
                ctx.redirect("/dashboard.html");
            } else {
                // Invalid login
                ctx.status(401).result("Invalid credentials. Please try again.");
            }
        } else {
            ctx.status(400).result("Username or password missing.");
        }
    }

    private static Map<String, String> getUserInfo(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        // Return the credentials if both are present
        if (username != null && password != null) {
            Map<String, String> userCredentials = new HashMap<>();
            userCredentials.put("username", username);
            userCredentials.put("password", password);
            return userCredentials;
        } else {
            return null; // Return null if any parameter is missing
        }
    }

    private static String validateUserCredentials(String username, String password) {
        String role = null;
        Connection connection = null;

        try {
            // Establish database connection
            connection = Database.getConnection();

            // SQL query to check user existence and get the role
            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            // Execute query
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // Get the role from the result set if the user exists
                role = resultSet.getString("role");
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Print stack trace for debugging
        } finally {
            try {
                if (connection != null) {
                    connection.close(); // Ensure connection is closed
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return role; // Return the role if the user exists, or null if not
    }
}
