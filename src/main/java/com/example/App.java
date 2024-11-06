package com.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class App {
    public static void main(String[] args) {
        // Test database connection on startup
        Database.testConnection();

        // Initialize Javalin app
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            // Add CORS if needed
            // config.enableCorsForAllOrigins();
        }).start(7000);

        // Basic routes
        setupBasicRoutes(app);

        // Authentication routes
        setupAuthRoutes(app);

        // API routes
        setupApiRoutes(app);

        // Protected routes
        setupProtectedRoutes(app);
    }

    private static void setupBasicRoutes(Javalin app) {
        // Root redirect to login
        app.get("/", ctx -> ctx.redirect("/login"));

        // Login page route
        app.get("/login", ctx -> ctx.redirect("/login.html"));

        // Logout route
        app.get("/logout", ctx -> {
            ctx.sessionAttribute("user", null);
            ctx.sessionAttribute("role", null);
            ctx.removeCookie("JSESSIONID");
            ctx.redirect("/login.html");
        });
    }

    private static void setupAuthRoutes(Javalin app) {
        // Login POST handler
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            // Validate input
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                ctx.redirect("/login.html?error=invalid_input");
                return;
            }

            try {
                Optional<Map<String, String>> userDetails = Database.authenticateUser(username, hashPassword(password));

                if (userDetails.isPresent()) {
                    Map<String, String> user = userDetails.get();

                    // Set session attributes
                    ctx.sessionAttribute("user", user.get("username"));
                    ctx.sessionAttribute("role", user.get("role"));

                    // Update last login time
                    Database.updateLastLogin(username);

                    // Redirect based on role
                    if ("admin".equalsIgnoreCase(user.get("role"))) {
                        ctx.redirect("/admin-dashboard.html");
                    } else {
                        ctx.redirect("/user-dashboard.html");
                    }
                } else {
                    ctx.redirect("/login.html?error=invalid_credentials");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.redirect("/login.html?error=server_error");
            }
        });

        // Registration route (if needed)
        app.post("/register", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            String confirmPassword = ctx.formParam("confirmPassword");

            // Basic validation
            if (username == null || password == null || !password.equals(confirmPassword)) {
                ctx.redirect("/register.html?error=invalid_input");
                return;
            }

            try {
                // Check if user already exists
                if (Database.userExists(username)) {
                    ctx.redirect("/register.html?error=user_exists");
                    return;
                }

                // Create new user with default role
                boolean created = Database.createUser(username, hashPassword(password), "user");

                if (created) {
                    ctx.redirect("/login.html?message=registration_success");
                } else {
                    ctx.redirect("/register.html?error=registration_failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.redirect("/register.html?error=server_error");
            }
        });
    }

    private static void setupApiRoutes(Javalin app) {
        // Get all tables (admin only)
        app.get("/api/tables", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.status(401);
                return;
            }

            if (!"admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.status(403);
                return;
            }

            try (Connection connection = Database.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement("SHOW TABLES");
                ResultSet rs = stmt.executeQuery();
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
                ctx.json(tables);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        // Get user data
        app.get("/api/user-data", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.status(401);
                return;
            }

            String username = ctx.sessionAttribute("user");
            Map<String, Object> userData = Database.getUserData(username);

            if (!userData.isEmpty()) {
                ctx.json(userData);
            } else {
                ctx.status(404);
            }
        });

        // Admin specific API to manage users (example)
        app.get("/api/users", ctx -> {
            if (!isAuthenticated(ctx) || !"admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.status(403);
                return;
            }

            try (Connection connection = Database.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT username, role, created_at FROM users"
                );
                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> users = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", rs.getString("username"));
                    user.put("role", rs.getString("role"));
                    user.put("created_at", rs.getTimestamp("created_at"));
                    users.add(user);
                }
                ctx.json(users);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });
    }

    private static void setupProtectedRoutes(Javalin app) {
        // Protect admin dashboard
        app.before("/admin-dashboard.html", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.redirect("/login.html");
                return;
            }
            if (!"admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.redirect("/user-dashboard.html");
            }
        });

        // Protect user dashboard
        app.before("/user-dashboard.html", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.redirect("/login.html");
                return;
            }
            // Optionally, redirect admins to admin dashboard
            if ("admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.redirect("/admin-dashboard.html");
            }
        });

        // Protect all API routes
        app.before("/api/*", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.status(401);
            }
        });
    }

    // Helper Methods
    private static boolean isAuthenticated(Context ctx) {
        return ctx.sessionAttribute("user") != null;
    }

    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    private static void setupFootballApiRoutes(Javalin app) {
        // Teams API endpoints
        app.get("/api/teams", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.status(401);
                return;
            }

            try (Connection conn = Database.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM teams ORDER BY points DESC"
                );
                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> teams = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> team = new HashMap<>();
                    team.put("team_id", rs.getInt("team_id"));
                    team.put("team_name", rs.getString("team_name"));
                    team.put("points", rs.getInt("points"));
                    team.put("matches_played", rs.getInt("matches_played"));
                    team.put("wins", rs.getInt("wins"));
                    team.put("draws", rs.getInt("draws"));
                    team.put("losses", rs.getInt("losses"));
                    teams.add(team);
                }
                ctx.json(teams);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        // Add new team (Admin only)
        app.post("/api/teams", ctx -> {
            if (!isAuthenticated(ctx) || !"admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.status(403);
                return;
            }

            try {
                String teamName = ctx.formParam("team_name");
                if (teamName == null || teamName.trim().isEmpty()) {
                    ctx.status(400).result("Team name is required");
                    return;
                }

                try (Connection conn = Database.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO teams (team_name, points, matches_played, wins, draws, losses) VALUES (?, 0, 0, 0, 0, 0)"
                    );
                    stmt.setString(1, teamName);
                    stmt.executeUpdate();
                    ctx.status(201).result("Team created successfully");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        // Matches API endpoints
        app.get("/api/matches", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.status(401);
                return;
            }

            try (Connection conn = Database.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT m.*, t1.team_name as home_team_name, t2.team_name as away_team_name " +
                                "FROM matches m " +
                                "JOIN teams t1 ON m.home_team_id = t1.team_id " +
                                "JOIN teams t2 ON m.away_team_id = t2.team_id " +
                                "ORDER BY match_date DESC"
                );
                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> matches = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("match_id", rs.getInt("match_id"));
                    match.put("home_team", rs.getString("home_team_name"));
                    match.put("away_team", rs.getString("away_team_name"));
                    match.put("home_score", rs.getInt("home_score"));
                    match.put("away_score", rs.getInt("away_score"));
                    match.put("match_date", rs.getTimestamp("match_date"));
                    match.put("status", rs.getString("status"));
                    matches.add(match);
                }
                ctx.json(matches);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        // Add new match (Admin only)
        app.post("/api/matches", ctx -> {
            if (!isAuthenticated(ctx) || !"admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.status(403);
                return;
            }

            try {
                int homeTeamId = Integer.parseInt(ctx.formParam("home_team_id"));
                int awayTeamId = Integer.parseInt(ctx.formParam("away_team_id"));
                String matchDate = ctx.formParam("match_date");

                try (Connection conn = Database.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO matches (home_team_id, away_team_id, match_date, status) " +
                                    "VALUES (?, ?, ?, 'SCHEDULED')"
                    );
                    stmt.setInt(1, homeTeamId);
                    stmt.setInt(2, awayTeamId);
                    stmt.setString(3, matchDate);
                    stmt.executeUpdate();
                    ctx.status(201).result("Match scheduled successfully");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        // Update match result (Admin only)
        app.put("/api/matches/:id/result", ctx -> {
            if (!isAuthenticated(ctx) || !"admin".equalsIgnoreCase(ctx.sessionAttribute("role"))) {
                ctx.status(403);
                return;
            }

            try {
                int matchId = Integer.parseInt(ctx.pathParam("id"));
                int homeScore = Integer.parseInt(ctx.formParam("home_score"));
                int awayScore = Integer.parseInt(ctx.formParam("away_score"));

                try (Connection conn = Database.getConnection()) {
                    // Start transaction
                    conn.setAutoCommit(false);

                    // Update match result
                    PreparedStatement updateMatch = conn.prepareStatement(
                            "UPDATE matches SET home_score = ?, away_score = ?, status = 'COMPLETED' " +
                                    "WHERE match_id = ?"
                    );
                    updateMatch.setInt(1, homeScore);
                    updateMatch.setInt(2, awayScore);
                    updateMatch.setInt(3, matchId);
                    updateMatch.executeUpdate();

                    // Update team statistics
                    updateTeamStats(conn, matchId, homeScore, awayScore);

                    conn.commit();
                    ctx.result("Match result updated successfully");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        // Get league standings
        app.get("/api/standings", ctx -> {
            if (!isAuthenticated(ctx)) {
                ctx.status(401);
                return;
            }

            try (Connection conn = Database.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM teams ORDER BY points DESC, wins DESC, (goals_for - goals_against) DESC"
                );
                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> standings = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> team = new HashMap<>();
                    team.put("position", standings.size() + 1);
                    team.put("team_name", rs.getString("team_name"));
                    team.put("matches_played", rs.getInt("matches_played"));
                    team.put("wins", rs.getInt("wins"));
                    team.put("draws", rs.getInt("draws"));
                    team.put("losses", rs.getInt("losses"));
                    team.put("goals_for", rs.getInt("goals_for"));
                    team.put("goals_against", rs.getInt("goals_against"));
                    team.put("goal_difference", rs.getInt("goals_for") - rs.getInt("goals_against"));
                    team.put("points", rs.getInt("points"));
                    standings.add(team);
                }
                ctx.json(standings);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });
    }

    // Helper method to update team statistics after a match
    private static void updateTeamStats(Connection conn, int matchId, int homeScore, int awayScore)
            throws Exception {
        // Get match details
        PreparedStatement getMatch = conn.prepareStatement(
                "SELECT home_team_id, away_team_id FROM matches WHERE match_id = ?"
        );
        getMatch.setInt(1, matchId);
        ResultSet matchRs = getMatch.executeQuery();

        if (matchRs.next()) {
            int homeTeamId = matchRs.getInt("home_team_id");
            int awayTeamId = matchRs.getInt("away_team_id");

            // Update home team stats
            updateTeam(conn, homeTeamId, homeScore, awayScore, true);
            // Update away team stats
            updateTeam(conn, awayTeamId, awayScore, homeScore, false);
        }
    }

    private static void updateTeam(Connection conn, int teamId, int goalsFor, int goalsAgainst,
                                   boolean isHome) throws Exception {
        String sql = "UPDATE teams SET" +
                "matches_played = matches_played + 1, " +
                "goals_for = goals_for + ?, " +
                "goals_against = goals_against + ?, " +
                "wins = wins + ?, " +
                "draws = draws + ?, " +
                "losses = losses + ?, " +
                "points = points + ? " +
                "WHERE team_id = ?";

        int wins = 0, draws = 0, losses = 0, points = 0;

        if (goalsFor > goalsAgainst) {
            wins = 1;
            points = 3;
        } else if (goalsFor == goalsAgainst) {
            draws = 1;
            points = 1;
        } else {
            losses = 1;
        }

        PreparedStatement updateTeam = conn.prepareStatement(sql);
        updateTeam.setInt(1, goalsFor);
        updateTeam.setInt(2, goalsAgainst);
        updateTeam.setInt(3, wins);
        updateTeam.setInt(4, draws);
        updateTeam.setInt(5, losses);
        updateTeam.setInt(6, points);
        updateTeam.setInt(7, teamId);
        updateTeam.executeUpdate();
    }
}

