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
import com.google.gson.JsonObject;
import io.javalin.http.sse.SseClient;


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

        app.get("/api/player-stats", ctx -> {
            List<Map<String, Object>> playerStatistics = Database.getPlayerStatistics();
            ctx.json(playerStatistics);
        });

        // Add new endpoint to handle user registration
        app.post("/register-user", ctx -> {
            try {
                // Parse incoming JSON into a User object
                User newUser = ctx.bodyAsClass(User.class);

                // Call the database function to add the user
                boolean isSuccess = Database.addUser(newUser.getUsername(), newUser.getPassword(), newUser.getRole());

                if (isSuccess) {
                    ctx.status(200).json(Map.of("message", "User registered successfully"));
                } else {
                    ctx.status(500).json(Map.of("error", "Failed to register user"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });
        app.post("/update-user", ctx -> {
            try {
                // Parse incoming JSON into a User object
                User updatedUser = ctx.bodyAsClass(User.class);

                // Call the database function to update the user
                boolean isSuccess = Database.updateUser(updatedUser.getUsername(), updatedUser.getPassword(), updatedUser.getRole());

                if (isSuccess) {
                    ctx.status(200).json(Map.of("message", "User updated successfully"));
                } else {
                    ctx.status(404).json(Map.of("error", "User not found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });

        app.post("/update-referee", ctx -> {
            try {
                // Parse incoming JSON into a Referee object
                Database.Referee referee = ctx.bodyAsClass(Database.Referee.class);

                // Call the database function to update the referee
                boolean isSuccess = Database.updateReferee(referee);

                if (isSuccess) {
                    ctx.status(200).json(Map.of("message", "Referee updated successfully"));
                } else {
                    ctx.status(404).json(Map.of("error", "Referee not found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });


        app.post("/add-referee", ctx -> {
            try {
                // Parse incoming JSON into a Referee object
                Database.Referee referee = ctx.bodyAsClass(Database.Referee.class);

                // Call the database function to add the referee
                boolean isAdded = Database.addReferee(referee);

                if (isAdded) {
                    ctx.status(201).json(Map.of("message", "Referee added successfully"));
                } else {
                    ctx.status(400).json(Map.of("error", "Failed to add referee"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });

        app.post("/update-contract", ctx -> {
            try {
                // Parse incoming JSON into a Contract object
                Database.Contract contract = ctx.bodyAsClass(Database.Contract.class);

                // Call the database function to update the contract
                boolean isSuccess = Database.updateContract(contract);

                if (isSuccess) {
                    ctx.status(200).json(Map.of("message", "Contract updated successfully"));
                } else {
                    ctx.status(404).json(Map.of("error", "Contract not found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });

        app.post("/update-team-staff", ctx -> {
            try {
                // Parse the incoming JSON request into a TeamStaff object
                Database.TeamStaff updatedStaff = ctx.bodyAsClass(Database.TeamStaff.class);

                // Call the database function to update the team staff record
                boolean isUpdated = Database.updateTeamStaff(updatedStaff);

                // Send the appropriate response based on whether the update was successful
                if (isUpdated) {
                    ctx.status(200).json(Map.of("message", "Team staff updated successfully"));
                } else {
                    ctx.status(404).json(Map.of("error", "Team staff not found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });

        app.post("/update-player", ctx -> {
            try {
                // Parse incoming JSON into a Player object
                Database.Player updatedPlayer = ctx.bodyAsClass(Database.Player.class);

                // Call the database function to update the player
                boolean isUpdated = Database.updatePlayer(updatedPlayer);

                // Respond with a success or error message based on the result
                if (isUpdated) {
                    ctx.status(200).json(Map.of("message", "Player updated successfully"));
                } else {
                    ctx.status(404).json(Map.of("error", "Player not found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });

        app.post("/add-player", ctx -> {
            try {
                // Parse incoming JSON into a Player object
                Map<String, String> playerData = ctx.bodyAsClass(Map.class);

                String name = playerData.get("name");
                Integer contractId = playerData.containsKey("contractId") ? Integer.parseInt(playerData.get("contractId")) : null;
                String nationality = playerData.get("nationality");
                Integer jerseyNumber = Integer.parseInt(playerData.get("jerseyNumber"));
                String position = playerData.get("position");
                String dob = playerData.get("dob");

                // Call the database function to add the player
                boolean isAdded = Database.addPlayer(name, contractId, nationality, jerseyNumber, position, dob);

                if (isAdded) {
                    ctx.status(201).json(Map.of("message", "Player added successfully"));
                } else {
                    ctx.status(500).json(Map.of("error", "Failed to add player"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
        });
        app.post("/update-player-stats", ctx -> {
            try {
                // Parse incoming JSON into a Map object
                System.out.println("before parse");
                Map<String, Object> playerStatsData = ctx.bodyAsClass(Map.class);

                int playerId = Integer.parseInt(playerStatsData.get("playerId").toString());
                int matchId = Integer.parseInt(playerStatsData.get("matchId").toString());
                int goalsScored = Integer.parseInt(playerStatsData.get("goalsScored").toString());
                int assists = Integer.parseInt(playerStatsData.get("assists").toString());
                String healthStatus = playerStatsData.get("healthStatus").toString();
                int yellowCards = Integer.parseInt(playerStatsData.get("yellowCards").toString());
                int redCards = Integer.parseInt(playerStatsData.get("redCards").toString());
                int tackles = Integer.parseInt(playerStatsData.get("tackles").toString());
                System.out.println("after parse before update");
                // Call the database function to update player statistics
                boolean isUpdated = Database.updatePlayerStatistics(playerId, matchId, goalsScored, assists, healthStatus,
                        yellowCards, redCards, tackles);
                System.out.println("after update");
                if (isUpdated) {
                    ctx.status(200).json(Map.of("message", "Player statistics updated successfully"));
                } else {
                    ctx.status(500).json(Map.of("error", "Failed to update player statistics"));
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log the exception
                ctx.status(400).json(Map.of("error", "Invalid request payload"));
            }
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

    // Function to handle user registration
    private static void handleUserRegistration(Context ctx) {
        JsonObject response = new JsonObject();

        try {
            // Parse JSON body
            JsonObject json = ctx.bodyAsClass(JsonObject.class);

            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            String role = json.get("role").getAsString();

            // Call database method to add user
            Database db = new Database();
            boolean success = db.addUser(username, password, role);

            if (success) {
                response.addProperty("message", "User registered successfully!");
                ctx.status(200).json(response);
            } else {
                response.addProperty("error", "Failed to register user.");
                ctx.status(500).json(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("error", "An unexpected error occurred: " + e.getMessage());
            ctx.status(500).json(response);
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

    public static class User {
        private String username;
        private String password;
        private String role;

        // Getters and setters
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getRole() {
            return role;
        }
        public void setRole(String role) {
            this.role = role;
        }
    }

}
