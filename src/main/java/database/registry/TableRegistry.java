package database.registry;

import annotations.Table;
import database.mapper.EntityMapper;
import database.mapper.TableMapper;
import database.util.ConnectionUtil;
import exceptions.NotFoundException;
import org.reflections.Reflections;

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
            String request = "CREATE TABLE " + tableName + " (" + fieldsToString(getClassByTableName(tableName,tableClasses)) + ")";
            Statement statement = ConnectionUtil.getConnection().createStatement();
            statement.execute(request);
        }
    }

    public Set<Class<?>> loadAllTableClasses() {
        Reflections reflections = new Reflections("model");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Table.class);
        return classes;
    }

    private List<String> loadAllTableNames(Set<Class<?>> tableClasses) {
        return tableClasses.stream().map(c -> {
            Table table = (Table) c.getAnnotation(Table.class);
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
        TableMapper tableMapper = new TableMapper(clazz);
        return tableMapper.fieldsToString();
    }


}
