package database.mapper.entityMapper;

import java.sql.Connection;

public class EntityMapperFactory {

    private static EntityMapperFactory entityMapperFactory;

    private EntityMapperFactory() {}

    public static EntityMapperFactory getEntityMapperFactory() {
        if(entityMapperFactory==null) {
            entityMapperFactory = new EntityMapperFactory();
        }
        return entityMapperFactory;
    }

    public <T> EntityMapper<T> getNewEntityMapper(T entity, Connection connection) throws InstantiationException, IllegalAccessException {
        return new EntityMapper<>(entity,connection);
    }

}
