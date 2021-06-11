package database.util.entityUtil;

import java.util.logging.Logger;

public class EntityUtilFactory {

    private Logger LOG = Logger.getLogger(EntityUtilFactory.class.getName());
    private static EntityUtilFactory entityUtilFactory;

    private EntityUtilFactory() {}

    public static EntityUtilFactory getEntityUtilFactory() {
        if(entityUtilFactory==null) {
            entityUtilFactory = new EntityUtilFactory();
        }
        return entityUtilFactory;
    }

    public <T> EntityUtil<T> getNewEntityUtil(T entity) {
        LOG.info("Creating EntityUtil for entity " + entity);
        return new EntityUtil<>(entity);
    }

}
