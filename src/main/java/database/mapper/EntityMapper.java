package database.mapper;

import annotations.*;
import database.registry.BasicTypeRegistry;
import database.util.ConnectionUtil;
import database.util.EntityUtil;
import database.util.RequestUtil;
import javafx.scene.control.Tab;

import javax.swing.border.EmptyBorder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EntityMapper<T> implements Mapper {

    private T entity;
    private String tableName;
    private List<Field> fields;
    private Map<String, String> columnNames;

    private Connection connection;

    public EntityMapper(T entity, Connection connection) throws IllegalAccessException, InstantiationException {
        this.entity = entity;

        EntityUtil<T> entityUtil = new EntityUtil(entity);
        this.tableName = entityUtil.getTableName();
        this.fields = entityUtil.getFields();
        this.columnNames = entityUtil.getColumnNames();

        this.connection = connection;
    }

    /* ------------------------------------------------ */

    public String mapEntityToInsertSqlRequest() throws IllegalAccessException {
        String request = "INSERT INTO " + tableName + " (" + fieldsToString() + ") VALUES " + "(" + valueFieldsToString() + ")";
        return request;
    }

    public String fieldsToString() throws IllegalAccessException {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> types = BasicTypeRegistry.getTypes();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getAnnotation(OneToOne.class)!=null && field.get(entity)==null) {
                continue;
            } else {
                stringBuilder.append(columnNames.get(field.getName()) + ",");
            }
        }

        return RequestUtil.deleteExtraComma(stringBuilder);
    }

    public String valueFieldsToString() throws IllegalAccessException {
        StringBuilder stringBuilder = new StringBuilder();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getAnnotation(OneToOne.class) != null) {
                Object value = field.get(entity);
                if (value != null) {
                    EntityUtil entityUtil = new EntityUtil(value);
                    stringBuilder.append(entityUtil.getIdField().get(value));
                }
            } else {
                if (field.getType().getTypeName().equals("java.lang.String")) {
                    stringBuilder.append(String.format("'%s'", field.get(entity)) + ",");
                } else {
                    stringBuilder.append(field.get(entity) + ",");
                }
            }
        }

        return RequestUtil.deleteExtraComma(stringBuilder);
    }

    /* ------------------------------------------------- */

    public List<T> mapResultSetToListEntity(ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<T> result = new LinkedList<>();
        boolean read = false;

        while (read = resultSet.next()) {
            result.add(mapResultSetToEntity(resultSet));
        }

        return result;
    }

    public T mapResultSetToEntity(ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        int i = 0;

        while (i <= fields.size() - 1) {
            Field field = fields.get(i);
            field.setAccessible(true);
            if (field.getAnnotation(OneToOne.class) != null) {
                Integer innerId = resultSet.getInt(columnNames.get(field.getName()));
                if (innerId != 0) {
                    EntityUtil entityUtil = new EntityUtil(field.getType().newInstance());
                    String request = "SELECT * FROM " + field.getType().getAnnotation(Table.class).name() + " WHERE " + entityUtil.getIdField().getAnnotation(Column.class).name() + "=" + innerId;
                    Statement statement = connection.createStatement();
                    statement.execute(request);
                    statement.getResultSet().next();
                    EntityMapper entityMapper = new EntityMapper(field.getType().newInstance(),connection);
                    field.set(entity, entityMapper.mapResultSetToEntity(statement.getResultSet()));
                }
            } else {
                if (field.getType().getTypeName().equals("java.lang.Integer")) {
                    field.set(entity, resultSet.getInt(columnNames.get(field.getName())));
                } else if (field.getType().getTypeName().equals("java.lang.String")) {
                    field.set(entity, resultSet.getString(columnNames.get(field.getName())));
                } else if (field.getType().getTypeName().equals("java.lang.Boolean")) {
                    field.set(entity, resultSet.getBoolean(columnNames.get(field.getName())));
                }
            }
            i++;
        }

        return entity;
    }

}
