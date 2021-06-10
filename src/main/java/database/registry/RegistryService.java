package database.registry;

import java.sql.SQLException;

public class RegistryService {

    public static void startRegistryServices() {
        new Thread(() ->
                BasicTypeRegistry.setTypes()
        ).start();

        TableRegistry tableRegistry = TableRegistry.getTableRegistry();
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
    }

}
