package com.example.demo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.example.demo.config.DataSourceConfig;

public class DB {

    public static Connection connect() throws SQLException {
        try {
            // Get database credentials from DatabaseConfig class
            var jdbcUrl = DataSourceConfig.getDbUrl();
            var user = DataSourceConfig.getDbUsername();
            var password = DataSourceConfig.getDbPassword();

            // Validate parameters
            if (jdbcUrl == null || user == null || password == null) {
                System.err.printf("Invalid database parameters %s, %s, %s", jdbcUrl, user, password);
                throw new SQLException("Invalid database parameters");
            }

            // Open a connection
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}