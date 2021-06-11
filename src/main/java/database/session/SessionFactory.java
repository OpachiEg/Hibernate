package database.session;


import java.sql.SQLException;
import java.util.logging.Logger;

public class SessionFactory {

    private static final Logger LOG = Logger.getLogger(SessionFactory.class.getName());

    public static Session createNewSession() throws SQLException {
        LOG.info("Creating new session");
        return new Session();
    }

}
