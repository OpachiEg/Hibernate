package database.mapper.tableMapper;

import annotations.Column;
import annotations.Id;
import annotations.OneToOne;
import annotations.Table;
import database.mapper.Mapper;
import database.registry.BasicTypeRegistry;
import database.util.entityUtil.EntityUtil;
import database.util.entityUtil.EntityUtilFactory;
import database.util.requestUtil.RequestUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TableMapper<T> implements Mapper {

    private T entity;
    private String tableName;
    private List<Field> fields;
    private Map<String,String> columnNames;

    protected TableMapper(T entity) {
        this.entity = entity;

        EntityUtil<T> entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(entity);
        this.tableName = entityUtil.getTableName();
        this.fields = entityUtil.getFields();
        this.columnNames = entityUtil.getColumnNames();
    }

    public String fieldsToString() throws IllegalAccessException, InstantiationException {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, String> types = BasicTypeRegistry.getTypes();

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation annotation = field.getAnnotation(Id.class);
            Annotation annotation1 = field.getAnnotation(OneToOne.class);
            if (annotation != null) {
                stringBuilder.append(columnNames.get(field.getName()) + " BIGINT PRIMARY KEY,");
            }
            else if (annotation1 != null) {
                Class<?> clazz = field.getType();
                Table table = clazz.getAnnotation(Table.class);
                EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(clazz.newInstance());
                Column column = entityUtil.getIdField().getAnnotation(Column.class);
                stringBuilder.append(columnNames.get(field.getName()) + " BIGINT REFERENCES " + table.name() + "(" + column.name() + ")" );
            }
            else {
                stringBuilder.append(columnNames.get(field.getName()) + " " + types.get(field.getType().getTypeName()) + ",");
            }
        }

        return RequestUtil.deleteExtraComma(stringBuilder);
    }

    public void getAllNotCreatedFieldNames(ResultSet resultSet) throws SQLException {
        for(int i=0;i<=fields.size()-1;i++) {
            if(columnNames.get(fields.get(i).getName()).equals(resultSet.getString("column_name"))) {
                fields.remove(i);
            }
        }
    }

    public List<Field> getFields() {
        return this.fields;
    }

}
