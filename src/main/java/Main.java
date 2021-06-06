import database.session.Session;
import database.registry.BasicTypeRegistry;
import model.User;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, SQLException, NoSuchFieldException {
        User user = new User();
        user.setId(6);
        user.setLogin("Egor5");
        user.setActive(true);

        BasicTypeRegistry.setTypes();

        Session session = new Session();
        session.createReadWriteTransaction();
        try {
            System.out.println(session.find(User.class,"id",1));
        } catch (Exception e) {
            session.rollbackTransaction();
            e.printStackTrace();
        }
        session.commitTransaction();
        session.close();
    }

}
