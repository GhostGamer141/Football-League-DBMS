package com.example;

import io.javalin.Javalin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Check if the database connection is successful
        Database.testConnection();

        // Start the Javalin server
        Javalin app = Javalin.create().start(7000);

        // Endpoint to display all tables in the database
        app.get("/", ctx -> {
            List<String> tables = new ArrayList<>();
            try (Connection connection = Database.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement("SHOW TABLES");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tables.add(rs.getString(1));  // Get the table name
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ctx.html("<h1>Tables in football_league_dbms Database</h1>" + String.join("<br>", tables));
        });
    }
}


