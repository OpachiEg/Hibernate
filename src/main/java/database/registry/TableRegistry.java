package database.registry;

import annotations.Column;
import annotations.OneToOne;
import annotations.Table;
import database.mapper.TableMapper;
import database.util.ConnectionUtil;
import database.util.EntityUtil;
import exceptions.NotFoundException;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TableRegistry {

    public void addAllTables() throws InstantiationException, IllegalAccessException, SQLException {
        Set<Class<?>> tableClasses = loadAllTableClasses();
        List<String> tableNames = loadAllTableNames(tableClasses);

        for(String tableName : tableNames) {
            String request = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + fieldsToString(getClassByTableName(tableName,tableClasses)) + ")";
            Statement statement = ConnectionUtil.getConnection().createStatement();
            statement.execute(request);
        }
    }

    public void addAllNotCreatedFields() throws SQLException, InstantiationException, IllegalAccessException {
        Set<Class<?>> tableClasses = loadAllTableClasses();
        List<String> tableNames = loadAllTableNames(tableClasses);

        for(String tableName : tableNames) {
            String request = "select column_name  from INFORMATION_SCHEMA.columns where table_name=" + "'" + tableName + "'";
            Statement statement = ConnectionUtil.getConnection().createStatement();
            statement.execute(request);
            ResultSet resultSet = statement.getResultSet();

            TableMapper tableMapper = new TableMapper(getClassByTableName(tableName,tableClasses).newInstance());
            boolean read = false;
            while(read = resultSet.next()) {
                tableMapper.getAllNotCreatedFieldNames(resultSet);
            }

            for(Object field : tableMapper.getFields()) {
                Field f = (Field) field;
                Annotation annotation = f.getAnnotation(OneToOne.class);
                if(annotation!=null) {
                    String request1 = "ALTER TABLE " + tableName + " ADD " + f.getAnnotation(Column.class).name() + " BIGINT";
                    Statement statement1 = ConnectionUtil.getConnection().createStatement();
                    statement1.execute(request1);

                    EntityUtil entityUtil = new EntityUtil(f.getType().newInstance());
                    String request2 = "ALTER TABLE " + tableName + " ADD FOREIGN KEY (" + f.getAnnotation(Column.class).name() + ") REFERENCES " + f.getType().getAnnotation(Table.class).name() + " (" + entityUtil.getColumnNames().get(entityUtil.getIdField().getName()) + ")" ;
                    System.out.println(request2);
                    Statement statement2 = ConnectionUtil.getConnection().createStatement();
                    statement2.execute(request2);
                }
                else {
                    String request1 = "ALTER TABLE " + tableName + " ADD " + f.getAnnotation(Column.class).name() + " " + BasicTypeRegistry.getTypes().get(f.getType().getTypeName());
                    Statement statement1 = ConnectionUtil.getConnection().createStatement();
                    statement1.execute(request1);
                }
            }
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
        TableMapper tableMapper = new TableMapper(clazz.newInstance());
        return tableMapper.fieldsToString();
    }


}
