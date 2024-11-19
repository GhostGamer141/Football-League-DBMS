package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Database {
    // Connection details
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASSWORD = "C3a#palmavenuegm";

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

    public static List<Map<String, Object>> getLeagueTable() {
        List<Map<String, Object>> leagueTable = new ArrayList<>();

        String query = """
            SELECT 
                t.Team_Name as team_name,
                ts.Matches_Played as played,
                ts.Wins as won,
                ts.Draws as drawn,
                ts.Losses as lost,
                ts.Points as points,
                ts.Goal_Difference as goal_difference,
                RANK() OVER (ORDER BY ts.Points DESC, ts.Goal_Difference DESC) as position
            FROM 
                team_standings ts
            INNER JOIN 
                teams t ON ts.Team_ID = t.Team_ID
            ORDER BY 
                ts.Points DESC,
                ts.Goal_Difference DESC,
                t.Team_Name ASC
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("team_name", rs.getString("team_name"));
                row.put("played", rs.getInt("played"));
                row.put("won", rs.getInt("won"));
                row.put("drawn", rs.getInt("drawn"));
                row.put("lost", rs.getInt("lost"));
                row.put("points", rs.getInt("points"));
                row.put("goal_difference", rs.getInt("goal_difference"));
                row.put("position", rs.getInt("position"));
                leagueTable.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving league table: " + e.getMessage());
            e.printStackTrace();
        }

        return leagueTable;
    }
}