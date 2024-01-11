package org.essential.core;

import org.apache.commons.lang3.ArrayUtils;
import org.essential.core.exceptions.OrmException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Session implements AutoCloseable {
    private final Connection connection;
    private final JdbcExecutor jdbcExecutor;

    private final Map<EntityKey, Object> enitiesMap = new HashMap<>();
    private final Map<EntityKey, Object[]> snapshotMap = new HashMap<>();

    public Session(DataSource dataSource) {
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new OrmException(e.getMessage(), e);
        }
        jdbcExecutor = new JdbcExecutor(connection);
    }

    @SuppressWarnings("unchecked")
    public <T> T find(Class<T> entityClass, Object id) {
        EntityKey key = EntityKey.builder()
                .entityId(id)
                .entityClass(entityClass)
                .build();

        return (T) enitiesMap.computeIfAbsent(key, this::load);
    }

    public void flush() {
        enitiesMap.forEach((this::updateIfDirty));
    }

    private void updateIfDirty(EntityKey entityKey, Object entity) {
        Object[] newSnapshot = EntityUtils.mapToSnapshot(entity, entityKey.getEntityClass());
        Object[] oldSnapshot = snapshotMap.get(entityKey);

        boolean isDirty = !Arrays.deepEquals(newSnapshot, oldSnapshot);

        if (isDirty) {
            String updateTemplate = EntityUtils.buildUpdateTemplate(entityKey.getEntityClass());
            Object[] bindValues = ArrayUtils.addAll(newSnapshot, entityKey.getEntityId());
            jdbcExecutor.executeUpdate(updateTemplate, bindValues);
        }
    }

    private Object load(EntityKey entityKey) {
        String sqlSelectTemplate = EntityUtils.buildSelectByIdTemplate(entityKey.getEntityClass());

        Object[] snapshot = jdbcExecutor.executeSelectQuery(sqlSelectTemplate, new Object[]{entityKey.getEntityId()})
                .stream()
                .findFirst()
                .orElseThrow(OrmException::new);

        Object entity = EntityUtils.mapToEntity(snapshot, entityKey.getEntityClass());
        snapshotMap.put(entityKey, snapshot);

        return entity;
    }

    @Override
    public void close() {
        flush();
        enitiesMap.clear();
        snapshotMap.clear();
        try {
            connection.close();
        } catch (SQLException e) {
            throw new OrmException(e.getMessage(), e.getCause());
        }
    }
}
