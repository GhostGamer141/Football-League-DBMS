package com.example;
import io.javalin.Javalin;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);

        app.post("/league-table", ctx -> {
            List<Map<String, Object>> standings = new ArrayList<>();

            // Database connection setup
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "C3a#palmavenuegm");
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SELECT * FROM team_standings");

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("team_id", rs.getInt("Team_ID"));
                    row.put("matches_played", rs.getInt("Matches_Played"));
                    row.put("wins", rs.getInt("Wins"));
                    row.put("losses", rs.getInt("Losses"));
                    row.put("draws", rs.getInt("Draws"));
                    row.put("points", rs.getInt("Points"));
                    row.put("goal_difference", rs.getInt("Goal_Difference"));
                    standings.add(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ctx.json(standings); // Send standings data as JSON
        });
    }
}
