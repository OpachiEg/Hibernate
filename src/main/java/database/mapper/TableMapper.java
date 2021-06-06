package database.mapper;

import annotations.Id;
import database.registry.BasicTypeRegistry;
import database.util.EntityUtil;
import database.util.RequestUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class TableMapper<T> implements Mapper {

    private T entity;
    private String tableName;
    private List<Field> fields;

    public TableMapper(T entity) throws IllegalAccessException, InstantiationException {
        this.entity = entity;

        EntityUtil<T> entityUtil = new EntityUtil(entity);
        this.tableName = entityUtil.getTableName();
        this.fields = entityUtil.getFields();
    }

    public String fieldsToString() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> types = BasicTypeRegistry.getTypes();

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                stringBuilder.append(field.getName() + " " + types.get(field.getType().getTypeName()) + " PRIMARY KEY,");
            } else {
                stringBuilder.append(field.getName() + " " + types.get(field.getType().getTypeName()) + ",");
            }
        }

        return RequestUtil.deleteExtraComma(stringBuilder);
    }

}
