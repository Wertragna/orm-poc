package org.essential.core;

import org.essential.annotations.Column;
import org.essential.annotations.Id;
import org.essential.annotations.Table;
import org.essential.core.exceptions.OrmException;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntityUtils {
    public static <T> T mapToEntity(Object[] snapshot, Class<T> entityClass) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            int i = 0;
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(entity, snapshot[i++]);
            }
            return entity;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new OrmException(e.getMessage(), e);
        }
    }

    public static Object[] mapToSnapshot(Object entity, Class<?> entityClass) {
        try {
            Field[] declaredFields = entityClass.getDeclaredFields();
            Object[] snapshot = new Object[declaredFields.length];
            int i = 0;
            for (Field field : declaredFields) {
                field.setAccessible(true);
                snapshot[i++] = field.get(entity);
            }
            return snapshot;
        } catch (IllegalAccessException e) {
            throw new OrmException(e.getMessage(), e);
        }
    }

    public static String buildSelectByIdTemplate(Class<?> entityClass) {
        String tableName = getTableName(entityClass);

        return "SELECT * FROM %s WHERE %s = ?"
                .formatted(tableName, getIdColumnName(entityClass));
    }

    public static String buildUpdateTemplate(Class<?> entityClass) {
        String tableName = getTableName(entityClass);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ").append(tableName).append(" SET ");
        String updateColumns = Arrays.stream(entityClass.getDeclaredFields())
                .map(EntityUtils::getColumnName)
                .map(columnName -> columnName + "=?")
                .collect(Collectors.joining(", "));
        stringBuilder.append(updateColumns)
                .append(" WHERE ")
                .append(getIdColumnName(entityClass)).append("=?");

        return stringBuilder.toString();
    }

    private static String getTableName(Class<?> entityClass) {
        return entityClass.getAnnotation(Table.class).name();
    }

    private static String getIdColumnName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(EntityUtils::getColumnName)
                .findFirst()
                .orElseThrow(HeadlessException::new);
    }

    private static String getColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .orElseThrow(HeadlessException::new);
    }
}
