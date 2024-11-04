package com.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Database.testConnection(); // Check DB connection

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(7000);

        app.get("/", ctx -> ctx.redirect("/login"));
        app.get("/login", ctx -> ctx.redirect("/login.html"));

        // Update this login route to use MySQL for authentication
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            if (username == null || password == null) {
                ctx.redirect("/login.html");
                return;
            }

            try (Connection connection = Database.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(hashPassword(password))) {
                        ctx.sessionAttribute("user", username);
                        ctx.redirect("/dashboard.html");
                        return;
                    }
                }
                ctx.redirect("/login.html");
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        app.get("/api/tables", ctx -> {
            if (ctx.sessionAttribute("user") == null) {
                ctx.status(401);
                return;
            }

            List<String> tables = new ArrayList<>();
            try (Connection connection = Database.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement("SHOW TABLES");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
                ctx.json(tables);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500);
            }
        });

        app.before("/dashboard.html", ctx -> {
            if (ctx.sessionAttribute("user") == null) {
                ctx.redirect("/login.html");
            }
        });

        app.get("/logout", ctx -> {
            ctx.sessionAttribute("user", null);
            ctx.redirect("/login.html");
        });
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
}
