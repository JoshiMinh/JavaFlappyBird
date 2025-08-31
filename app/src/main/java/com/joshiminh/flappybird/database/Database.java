package com.joshiminh.flappybird.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for creating database connections.
 *
 * <p>The connection details are loaded from environment variables or, if not
 * present, from a {@code db.properties} file on the classpath. The expected
 * keys are {@code DB_URL}, {@code DB_USERNAME} and {@code DB_PASSWORD}.</p>
 */
public class Database {
  private static String url;
  private static String username;
  private static String password;

  private static void loadConfig() {
    if (url != null && username != null && password != null) {
      return;
    }

    url = getConfig("DB_URL");
    username = getConfig("DB_USERNAME");
    password = getConfig("DB_PASSWORD");
  }

  private static String getConfig(String key) {
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      return value;
    }

    try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
      if (in != null) {
        Properties props = new Properties();
        props.load(in);
        value = props.getProperty(key);
        if (value != null && !value.isEmpty()) {
          return value;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load database configuration", e);
    }

    throw new IllegalStateException("Missing configuration for " + key);
  }

  public static Connection getConnection() throws SQLException {
    loadConfig();
    return DriverManager.getConnection(url, username, password);
  }
}
