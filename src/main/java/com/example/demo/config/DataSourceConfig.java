package com.example.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSourceConfig {
    private static final Properties properties = new Properties();

    private DataSourceConfig() {

    }

    static {

        try (InputStream input = ClassLoader.getSystemClassLoader()
                .getResourceAsStream("application.properties");) {
            if (input == null) {
                System.err.println("application.properties not found");
                throw new IllegalStateException("application.properties not found");
            }
            properties.load(input);
            System.out.println(properties.entrySet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("spring.datasource.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("spring.datasource.username");
    }

    public static String getDbPassword() {
        return properties.getProperty("spring.datasource.password");
    }

}
