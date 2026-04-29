package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                PROPERTIES.load(input);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static Connection getConnection() throws SQLException {
        String driver = PROPERTIES.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String url = PROPERTIES.getProperty(
        	    "db.url",
        	    "jdbc:mysql://localhost:3306/usc_marketplace?serverTimezone=America/Los_Angeles"
        	);        String username = trimOrEmpty(PROPERTIES.getProperty("db.username", "root"));
        String password = trimOrEmpty(PROPERTIES.getProperty("db.password", ""));

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found. Add mysql-connector-j to WEB-INF/lib or Tomcat lib.", e);
        }

        return DriverManager.getConnection(url, username, password);
    }
}