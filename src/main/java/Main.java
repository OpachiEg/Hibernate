import database.mapper.EntityMapper;
import database.registry.RegistryService;
import database.registry.TableRegistry;
import database.session.Session;
import database.registry.BasicTypeRegistry;
import database.util.ConnectionUtil;
import model.Passport;
import model.User;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, InstantiationException, IllegalAccessException {
        Passport passport = new Passport();
        passport.setId(1);

        User user = new User();
        user.setId(6);
        user.setLogin("Egor7");
        user.setActive(true);
        user.setPassport(passport);

        RegistryService.startRegistryServices();

        Session session = new Session();
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
