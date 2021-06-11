import database.registry.RegistryService;
import database.session.Session;
import database.session.SessionFactory;
import model.Passport;
import model.User;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {
        Passport passport = new Passport();
        passport.setId(1);

        User user = new User();
        user.setId(6);
        user.setLogin("Egor7");
        user.setActive(true);
        user.setPassport(passport);

        RegistryService.startRegistryServices();

        Session session = SessionFactory.createNewSession();
        session.createReadWriteTransaction();
        try {
            session.save(user);
        } catch (Exception e) {
            session.rollbackTransaction();
            e.printStackTrace();
        }
        session.commitTransaction();
        session.close();

    }

}
