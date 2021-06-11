package database.registry;

import annotations.Column;
import annotations.OneToOne;
import annotations.Table;
import database.mapper.tableMapper.TableMapper;
import database.mapper.tableMapper.TableMapperFactory;
import database.util.connectionUtil.ConnectionUtil;
import database.util.entityUtil.EntityUtil;
import database.util.entityUtil.EntityUtilFactory;
import exceptions.NotFoundException;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TableRegistry {

    private Connection connection;
    private static TableRegistry tableRegistry;

    private TableRegistry() {
        this.connection = ConnectionUtil.getNewConnection();
    }

    public static TableRegistry getTableRegistry() {
        if(tableRegistry==null) {
            return new TableRegistry();
        }
        return tableRegistry;
    }

    public void addAllTables() throws InstantiationException, IllegalAccessException, SQLException {
        Set<Class<?>> tableClasses = loadAllTableClasses();
        List<String> tableNames = loadAllTableNames(tableClasses);

        for(String tableName : tableNames) {
            String request = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + fieldsToString(getClassByTableName(tableName,tableClasses)) + ")";
            Statement statement = connection.createStatement();
            statement.execute(request);
        }
    }

    private Set<Class<?>> loadAllTableClasses() {
        Reflections reflections = new Reflections("model");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Table.class);
        return classes;
    }

    private List<String> loadAllTableNames(Set<Class<?>> tableClasses) {
        return tableClasses.stream().map(c -> {
            Table table = c.getAnnotation(Table.class);
            return table.name();
        }).collect(Collectors.toList());
    }

    public Class getClassByTableName(String tableName,Set<Class<?>> classes) {
        for(Class clazz : classes) {
            Table table = (Table) clazz.getAnnotation(Table.class);
            if(table.name().equals(tableName)) {
                return clazz;
            }
        }
        throw new NotFoundException(String.format("Table with name %s not found",tableName));
    }

    public String fieldsToString(Class clazz) throws IllegalAccessException, InstantiationException {
        TableMapper tableMapper = TableMapperFactory.getTableMapperFactory().getNewTableMapper(clazz.newInstance());
        return tableMapper.fieldsToString();
    }

    /* ------------------------------------- */

    public void addAllNotCreatedFields() throws SQLException, InstantiationException, IllegalAccessException {
        Set<Class<?>> tableClasses = loadAllTableClasses();
        List<String> tableNames = loadAllTableNames(tableClasses);

        for(String tableName : tableNames) {
            String request = "SELECT COLUMN_NAME  FROM INFORMATION_SCHEMA.columns WHERE TABLE_NAME=" + "'" + tableName + "'";
            Statement statement = connection.createStatement();
            statement.execute(request);
            ResultSet resultSet = statement.getResultSet();

            TableMapper tableMapper = TableMapperFactory.getTableMapperFactory().getNewTableMapper(getClassByTableName(tableName,tableClasses).newInstance());
            while(resultSet.next()) {
                tableMapper.getAllNotCreatedFieldNames(resultSet);
            }

            for(Object f : tableMapper.getFields()) {
                Field field = (Field) f;
                Annotation annotation = field.getAnnotation(OneToOne.class);
                if(annotation!=null) {
                    addColumn(tableName,field.getAnnotation(Column.class).name(),"BIGINT");

                    EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(field.getType().newInstance());
                    String request2 = "ALTER TABLE " + tableName + " ADD FOREIGN KEY (" + field.getAnnotation(Column.class).name() + ") REFERENCES " + field.getType().getAnnotation(Table.class).name() + " (" + entityUtil.getColumnNames().get(entityUtil.getIdField().getName()) + ")" ;
                    System.out.println(request2);
                    Statement statement2 = connection.createStatement();
                    statement2.execute(request2);
                    statement2.close();
                }
                else {
                    addColumn(tableName,field.getAnnotation(Column.class).name(),BasicTypeRegistry.getTypes().get(field.getType().getTypeName()));
                }
            }
        }
    }

    private void addColumn(String tableName, String columnName, String type) throws SQLException {
        String request = "ALTER TABLE " + tableName + " ADD " + columnName + " " + type;
        Statement statement = connection.createStatement();
        statement.execute(request);
        statement.close();
    }

}
