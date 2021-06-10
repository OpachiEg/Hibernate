import database.registry.TableRegistry;
import database.session.Session;
import database.registry.BasicTypeRegistry;
import model.User;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {

        User user = new User();
        user.setLogin("Egor7");
        user.setActive(true);

        new Thread(() ->
            BasicTypeRegistry.setTypes()
        ).start();

        TableRegistry tableRegistry = new TableRegistry();
        new Thread(() -> {
            try {
                tableRegistry.addAllTables();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                tableRegistry.addAllNotCreatedFields();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }).start();

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
