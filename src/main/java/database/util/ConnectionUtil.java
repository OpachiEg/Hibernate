package database.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtil {

    private static final String URL = "jdbc:postgresql://localhost:5430/hibernate";
    private static final String USER = "postgres";
    private static final String PASSWORD = "traveller0202";
    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static Connection setNewConnection() {
        try {
            connection = DriverManager.getConnection(URL,USER,PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
