package database.mapper.entityMapper;

import annotations.*;
import database.mapper.Mapper;
import database.util.entityUtil.EntityUtil;
import database.util.entityUtil.EntityUtilFactory;
import database.util.requestUtil.RequestUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityMapper<T> implements Mapper {

    private T entity;
    private String tableName;
    private List<Field> fields;
    private Field idField;
    private Map<String,String> columnNames;
    private Connection connection;

    protected EntityMapper(T entity, Connection connection) throws IllegalAccessException, InstantiationException {
        this.entity = entity;

        EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(entity);
        this.tableName = entityUtil.getTableName();
        this.fields = entityUtil.getFields();
        this.idField = entityUtil.getIdField();
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
                    EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(value);
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

    public String mapEntityToUpdateRequest() throws IllegalAccessException {
        StringBuilder stringBuilder = new StringBuilder("UPDATE " + tableName + " SET ");

        for(Object f : fields) {
            Field field = (Field) f;
            if(field.getAnnotation(Id.class)==null) {
                if (field.getAnnotation(OneToOne.class) != null) {
                    Object value = field.get(entity);
                    if (value != null) {
                        EntityUtil entityUtil1 = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(value);
                        stringBuilder.append(columnNames.get(field.getName()) + "=" + entityUtil1.getIdField().get(value) + ",");
                    }
                } else {
                    if (field.getType().getTypeName().equals("java.lang.String")) {
                        stringBuilder.append(columnNames.get(field.getName()) + "=" + "'" + field.get(entity) + "'" + ",");
                    }
                    else {
                        stringBuilder.append(columnNames.get(field.getName()) + "=" + field.get(entity) + ",");
                    }
                }
            }
        }

        stringBuilder = new StringBuilder(RequestUtil.deleteExtraComma(stringBuilder));
        stringBuilder.append(" WHERE " + idField.getAnnotation(Column.class).name() + "=" + idField.get(entity));

        return stringBuilder.toString();
    }

    /* ------------------------------------------------- */

    public List<T> mapResultSetToListEntity(ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<T> result = new LinkedList<>();

        while (resultSet.next()) {
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
                    EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(field.getType().newInstance());
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
