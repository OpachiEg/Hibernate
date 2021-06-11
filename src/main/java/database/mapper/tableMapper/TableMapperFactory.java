package database.mapper.tableMapper;

public class TableMapperFactory {

    private static TableMapperFactory tableMapperFactory;

    private TableMapperFactory() {}

    public static TableMapperFactory  getTableMapperFactory() {
        if(tableMapperFactory==null) {
            tableMapperFactory = new TableMapperFactory();
        }
        return tableMapperFactory;
    }

    public <T> TableMapper<T> getNewTableMapper(T entity) {
        return new TableMapper<>(entity);
    }

}
