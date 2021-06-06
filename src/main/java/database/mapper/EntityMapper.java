package database.mapper;

import annotations.Entity;
import annotations.Id;
import database.registry.BasicTypeRegistry;
import database.util.EntityUtil;
import database.util.RequestUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EntityMapper<T> implements Mapper {

    private T entity;
    private String tableName;
    private List<Field> fields;

    public EntityMapper(T entity) throws IllegalAccessException, InstantiationException {
        this.entity = entity;

        EntityUtil<T> entityUtil = new EntityUtil(entity);
        this.tableName = entityUtil.getTableName();
        this.fields = entityUtil.getFields();
    }

    /* ------------------------------------------------ */

    public String mapEntityToInsertSqlRequest() throws IllegalAccessException {
        String request = "INSERT INTO " + tableName + " (" + fieldsToString() + ") VALUES " + "(" + valueFieldsToString() + ")";
        return request;
    }

    public String fieldsToString() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> types = BasicTypeRegistry.getTypes();

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation annotation = field.getAnnotation(Id.class);
            stringBuilder.append(field.getName() + ",");
        }

        return RequestUtil.deleteExtraComma(stringBuilder);
    }

    private String valueFieldsToString() throws IllegalAccessException {
        StringBuilder stringBuilder = new StringBuilder();

        for (Field field : fields) {
            if (field.getType().getTypeName().equals("java.lang.String")) {
                stringBuilder.append(String.format("'%s'", field.get(entity)) + ",");
            } else {
                stringBuilder.append(field.get(entity) + ",");
            }
        }

        return RequestUtil.deleteExtraComma(stringBuilder);
    }

    /* ------------------------------------------------- */

    public List<T> mapResultSetToListEntity(ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        List<T> result = new LinkedList<>();
        boolean read = false;

        while(read=resultSet.next()) {
            result.add(mapResultSetToEntity(resultSet));
        }
        return result;
    }

    public T mapResultSetToEntity(ResultSet resultSet) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        T entity = this.entity;
        int i = 0;

        while (i <= fields.size() - 1) {
            Field field = fields.get(i);
            if (field.getType().getTypeName().equals("java.lang.Integer")) {
                Field f = entity.getClass().getDeclaredField(field.getName());
                f.setAccessible(true);
                f.set(entity, resultSet.getInt(field.getName()));
            } else if (field.getType().getTypeName().equals("java.lang.String")) {
                Field f = entity.getClass().getDeclaredField(field.getName());
                f.setAccessible(true);
                f.set(entity, resultSet.getString(field.getName()));
            } else if (field.getType().getTypeName().equals("java.lang.Boolean")) {
                Field f = entity.getClass().getDeclaredField(field.getName());
                f.setAccessible(true);
                f.set(entity, resultSet.getBoolean(field.getName()));
            }
            i++;
        }
        return entity;
    }


}
