package database.session;

import database.mapper.entityMapper.EntityMapperFactory;
import database.util.entityUtil.EntityUtilFactory;
import database.mapper.entityMapper.EntityMapper;
import database.util.connectionUtil.ConnectionUtil;
import database.util.entityUtil.EntityUtil;

import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

public class Session {

    private final Logger LOG = Logger.getLogger(Session.class.getName());
    private final Connection CONNECTION;

    protected Session() throws SQLException {
        LOG.info("Creating session");
        this.CONNECTION = ConnectionUtil.getNewConnection();
        this.CONNECTION.setAutoCommit(false);
    }

    /* ------------------------------------------------ */

    public void save(Object entity) throws SQLException, InstantiationException, IllegalAccessException {
        EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(entity);
        String tableName = entityUtil.getTableName();

        if((Integer) entityUtil.getIdField().get(entity)==0) {
            LOG.info("Creating entity " + entity);
            LOG.info("Setting id");
            entityUtil.getIdField().set(entity,getId(tableName));

            EntityMapper entityMapper = EntityMapperFactory.getEntityMapperFactory().getNewEntityMapper(entity,this.CONNECTION);
            String request = entityMapper.mapEntityToInsertSqlRequest();

            LOG.info("Creating statement");
            Statement statement = this.CONNECTION.createStatement();
            statement.execute(request);
        }
        else {
            update(entity);
        }
    }

    public void update(Object entity) throws InstantiationException, IllegalAccessException, SQLException {
        LOG.info("Updating entity " + entity);
        EntityMapper entityMapper = EntityMapperFactory.getEntityMapperFactory().getNewEntityMapper(entity,this.CONNECTION);
        String REQUEST = entityMapper.mapEntityToUpdateRequest();

        LOG.info("Creating statement");
        Statement statement = this.CONNECTION.createStatement();
        LOG.info("Executing request " + REQUEST);
        statement.execute(REQUEST);
    }

    public <T> T find(Class<T> clazz, String columnName, Object value) throws IllegalAccessException, InstantiationException, SQLException, NoSuchFieldException {
        LOG.info("Searching class with type " + clazz.getTypeName() + " by " + columnName + " with value " + value);

        EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(clazz.newInstance());
        String tableName = entityUtil.getTableName();

        LOG.info("Creating statement");
        Statement statement = this.CONNECTION.createStatement();
        String request = "SELECT * FROM " + tableName + " WHERE " + columnName + "=" + value;
        LOG.info("Executing request " + request);
        statement.execute(request);
        LOG.info("Getting ResultSet");
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();

        EntityMapper<T> entityMapper = EntityMapperFactory.getEntityMapperFactory().getNewEntityMapper(clazz.newInstance(),this.CONNECTION);
        return entityMapper.mapResultSetToEntity(resultSet);
    }

    public <T> List<T> findAll(Class<T> clazz) throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        LOG.info("Getting all entities with type " + clazz.getTypeName());

        EntityUtil entityUtil = EntityUtilFactory.getEntityUtilFactory().getNewEntityUtil(clazz.newInstance());
        final String TABLE_NAME = entityUtil.getTableName();

        LOG.info("Creating statement");
        Statement statement = this.CONNECTION.createStatement();
        final String REQUEST = "SELECT * FROM " + TABLE_NAME;
        LOG.info("Executing request " + REQUEST);
        statement.execute(REQUEST);
        LOG.info("Getting ResultSet");
        ResultSet resultSet = statement.getResultSet();

        EntityMapper<T> entityMapper = EntityMapperFactory.getEntityMapperFactory().getNewEntityMapper(clazz.newInstance(), this.CONNECTION);
        return entityMapper.mapResultSetToListEntity(resultSet);
    }

    /* ------------------------------------------------ */

    public void createReadWriteTransaction() throws SQLException {
        LOG.info("Creating save point for read write transaction");
        this.CONNECTION.setSavepoint();
    }

    public void createReadOnlyTransaction() throws SQLException {
        LOG.info("Creating save point for read only transaction");
        this.CONNECTION.setReadOnly(true);
        this.CONNECTION.setSavepoint();
    }

    public void commitTransaction() throws SQLException {
        LOG.info("Committing transaction");
        this.CONNECTION.commit();
    }

    public void rollbackTransaction() throws SQLException {
        LOG.info("Rollback transaction");
        this.CONNECTION.rollback();
    }

    /* ------------------------------------------------ */

    public void close() throws SQLException {
        LOG.info("Closing transaction");
        this.CONNECTION.close();
    }

    /* ------------------------------------------------ */

    private Integer getId(String tableName) throws SQLException {
        String request = "SELECT * FROM " + tableName;
        Statement statement = this.CONNECTION.createStatement();
        statement.execute(request);
        ResultSet resultSet = statement.getResultSet();
        int counter = 1;
        while(resultSet.next()) {
            counter++;
        }
        return counter;
    }

}
