package com.example.studentmanagement.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String host = setting("MYSQL_HOST", "localhost");
        String port = setting("MYSQL_PORT", "3306");
        String database = setting("MYSQL_DATABASE", "student_management");
        String username = setting("MYSQL_USER", "root");
        String password = System.getenv("MYSQL_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new SQLException("MYSQL_PASSWORD is not set. Set it in PowerShell before running the app or tests.");
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, username, password);
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
