package database.session;

import database.mapper.EntityMapper;
import database.util.ConnectionUtil;
import database.util.EntityUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

public class Session {

    private final Connection CONNECTION;
    private boolean flag = false;

    public Session() throws SQLException {
        this.CONNECTION = ConnectionUtil.setNewConnection();
        this.CONNECTION.setAutoCommit(false);
    }

    /* ------------------------------------------------ */

    public void save(Object entity) throws SQLException, InstantiationException, IllegalAccessException {
        EntityUtil entityUtil = new EntityUtil(entity);
        final String tableName = entityUtil.getTableName();

        entityUtil.getIdField().set(entity,getId(tableName));

        EntityMapper entityMapper = new EntityMapper(entity,this.CONNECTION);
        String request = entityMapper.mapEntityToInsertSqlRequest();

        Statement statement = this.CONNECTION.createStatement();
        statement.execute(request);
    }

    public <T> T find(Class<T> clazz, String columnName, Object value) throws IllegalAccessException, InstantiationException, SQLException, NoSuchFieldException {
        EntityUtil entityUtil = new EntityUtil(clazz.newInstance());
        final String TABLE_NAME = entityUtil.getTableName();

        Statement statement = this.CONNECTION.createStatement();
        statement.execute("SELECT * FROM " + TABLE_NAME + " WHERE " + columnName + "=" + value);
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();

        EntityMapper<T> entityMapper = new EntityMapper<>(clazz.newInstance(),this.CONNECTION);

        return entityMapper.mapResultSetToEntity(resultSet);
    }

    public <T> List<T> findAll(Class<T> clazz) throws SQLException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        EntityUtil entityUtil = new EntityUtil(clazz.newInstance());
        final String TABLE_NAME = entityUtil.getTableName();

        Statement statement = this.CONNECTION.createStatement();
        statement.execute("SELECT * FROM " + TABLE_NAME);
        ResultSet resultSet = statement.getResultSet();

        EntityMapper<T> entityMapper = new EntityMapper(clazz.newInstance(), this.CONNECTION);

        return entityMapper.mapResultSetToListEntity(resultSet);
    }

    /* ------------------------------------------------ */

    public void createReadWriteTransaction() throws SQLException {
        this.CONNECTION.setSavepoint();
    }

    public void createReadOnlyTransaction() throws SQLException {
        this.CONNECTION.setReadOnly(true);
        this.CONNECTION.setSavepoint();
    }

    public void commitTransaction() throws SQLException {
        this.CONNECTION.commit();
    }

    public void rollbackTransaction() throws SQLException {
        this.CONNECTION.rollback();
    }

    /* ------------------------------------------------ */

    public void close() throws SQLException {
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
