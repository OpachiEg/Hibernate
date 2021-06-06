package database.util;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.Table;
import exceptions.NotFoundException;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityUtil<T> {

    private T entity;
    private String tableName;
    private List<Field> fields=new LinkedList<>();
    //     1-имя поля, 2-имя столбца
    private Map<String,String> columnNames = new HashMap();

    public EntityUtil(T entity) {
        this.entity = entity;
        if(isEntity()) {
            setTableName();
            setFields();
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<Field> getFields() {
        return this.fields;
     }

    public Map<String,String> getColumnNames() {
        return this.columnNames;
    }

    public Field getIdField() {
        for(Field field : this.fields) {
            Annotation annotation = field.getAnnotation(Id.class);
            if(annotation!=null) {
                return field;
            }
        }
        throw new NotFoundException("Id annotation not found");
    }

    /* --------------------------------------------*/

    private boolean isEntity() {
        Annotation annotation = getAnnotationByClass(Entity.class);
        if(annotation==null) {
            throw new NotFoundException("Entity annotation not found");
        }
        return true;
    }

    private void setTableName() {
        tableName = ((Table) getAnnotationByClass(Table.class)).name();
    }

    private void setFields() {
        Field[] fields = entity.getClass().getDeclaredFields();

        for(Field field : fields) {
            field.setAccessible(true);
            Column column = ((Column) field.getAnnotation(Column.class));
            if(column!=null) {
                this.columnNames.put(field.getName(),column.name());
                this.fields.add(field);
            }
        }
    }

    private Annotation getAnnotationByClass(Class classAnnotations) {
        Annotation annotation = entity.getClass().getAnnotation(classAnnotations);
        return annotation;
    }

}
