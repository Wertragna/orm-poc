package org.essential.core;

import lombok.RequiredArgsConstructor;
import org.essential.core.exceptions.OrmException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JdbcExecutor {

    private final Connection connection;

    public List<Object[]> executeSelectQuery(String sqlTemplate, Object[] bindValues) {
        System.out.println(sqlTemplate + Arrays.toString(bindValues));
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlTemplate)) {
            List<Object[]> results = new ArrayList<>();
            bindParameters(preparedStatement, bindValues);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Object[] resultRow = extractResultRow(resultSet);
                    results.add(resultRow);
                }
            }
            return results;
        } catch (SQLException sqlException) {
            throw new OrmException(sqlException.getMessage(), sqlException);
        }
    }


    public int executeUpdate(String sqlTemplate, Object[] bindValues) {
        System.out.println(sqlTemplate + Arrays.toString(bindValues));
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlTemplate)) {
            bindParameters(preparedStatement, bindValues);
            return preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new OrmException(sqlException.getMessage(), sqlException);
        }
    }

    private void bindParameters(PreparedStatement preparedStatement, Object[] bindValues) throws SQLException {
        if (bindValues != null && bindValues.length > 0) {
            for (int i = 0; i < bindValues.length; i++) {
                preparedStatement.setObject(i + 1, bindValues[i]);
            }
        }
    }

    private Object[] extractResultRow(ResultSet resultSet) throws SQLException {
        Object[] resultRow = new Object[resultSet.getMetaData().getColumnCount()];
        for (int i = 0; i < resultRow.length; i++) {
            resultRow[i] = resultSet.getObject(i + 1);
        }
        return resultRow;
    }

}