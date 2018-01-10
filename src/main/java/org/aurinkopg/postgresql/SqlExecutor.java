package org.aurinkopg.postgresql;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.sql.ResultSet.*;

class SqlExecutor {
    static int executeSqlUpdate(String sql, Connection connection) throws SQLException {
        try (Statement statement =
                 connection.createStatement(
                     TYPE_FORWARD_ONLY,
                     CONCUR_READ_ONLY,
                     CLOSE_CURSORS_AT_COMMIT)) {
            return statement.executeUpdate(sql);
        }
    }

    static List<Map<String, Object>> executeSqlQuery(String sql, Connection connection) throws SQLException {
        try (Statement statement =
                 connection.createStatement(
                     TYPE_FORWARD_ONLY,
                     CONCUR_READ_ONLY,
                     CLOSE_CURSORS_AT_COMMIT)) {
            return resultSetToListOfMaps(statement.executeQuery(sql));
        }
    }

    private static List<Map<String, Object>> resultSetToListOfMaps(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> output = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            output.add(row);
        }
        return output;
    }
}
