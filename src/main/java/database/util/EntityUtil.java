package database.util;

import annotations.Column;
import annotations.Entity;
import annotations.Table;
import exceptions.EntityAnnotationNotFound;
import exceptions.NotFoundException;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class EntityUtil<T> {

    private T entity;
    private String tableName;
    private List<Field> fields=new LinkedList<>();

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

    public Field getFieldByColumnName(String name) {
        for(Field f : fields) {
            if(f.getName().equals(name)) {
                return f;
            }
        }

        throw new NotFoundException("Field not found");
    }

    /* --------------------------------------------*/

    private boolean isEntity() {
        Annotation annotation = getAnnotationByClass(Entity.class);
        if(annotation==null) {
            throw new EntityAnnotationNotFound("Entity annotation not found");
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
                this.fields.add(field);
            }
        }
    }

    private Annotation getAnnotationByClass(Class classAnnotations) {
        Annotation annotation = entity.getClass().getAnnotation(classAnnotations);
        return annotation;
    }

}
