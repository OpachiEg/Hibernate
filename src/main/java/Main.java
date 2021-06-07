import database.mapper.EntityMapper;
import database.mapper.TableMapper;
import database.registry.TableRegistry;
import database.session.Session;
import database.registry.BasicTypeRegistry;
import model.Passport;
import model.User;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, SQLException, NoSuchFieldException {
        Passport passport = new Passport();
        passport.setId(1L);

        User user = new User();
        user.setId(6);
        user.setLogin("Egor5");
        user.setActive(true);
        user.setPassport(passport);


        BasicTypeRegistry.setTypes();

        TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addAllTables();
        tableRegistry.addAllNotCreatedFields();

        /*
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

         */


    }

}
