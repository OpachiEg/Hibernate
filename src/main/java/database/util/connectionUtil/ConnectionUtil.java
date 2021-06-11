package database.util.connectionUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ConnectionUtil {

    private static final String URL = "jdbc:postgresql://localhost:5430/hibernate";
    private static final String USER = "postgres";
    private static final String PASSWORD = "traveller0202";
    private static Connection connection;
    private static final Logger LOG = Logger.getLogger(ConnectionUtil.class.getName());

    public static Connection getNewConnection() {
        try {
            LOG.info("Creating connection");
            connection = DriverManager.getConnection(URL,USER,PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
