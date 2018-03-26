package org.aurinkopg.postgresql;

import org.postgresql.jdbc.PgStatement;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.sql.ResultSet.*;
import static org.postgresql.core.QueryExecutor.QUERY_NO_RESULTS;

public class SqlExecutor {
    static void executeSqlUpdate(String sql, Connection connection) throws SQLException {
        PgStatement statement = (PgStatement)
            connection.createStatement(
                TYPE_FORWARD_ONLY,
                CONCUR_READ_ONLY,
                CLOSE_CURSORS_AT_COMMIT);
        statement.closeOnCompletion();
        statement.executeWithFlags(sql, QUERY_NO_RESULTS);
    }

    public static List<Map<String, Object>> executeSqlQuery(String sql, Connection connection) throws SQLException {
        Statement statement =
            connection.createStatement(
                TYPE_FORWARD_ONLY,
                CONCUR_READ_ONLY,
                CLOSE_CURSORS_AT_COMMIT);
        statement.closeOnCompletion();
        return resultSetToListOfMaps(statement.executeQuery(sql));
    }

    private static List<Map<String, Object>> resultSetToListOfMaps(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> output = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            output.add(row);
        }
        return output;
    }
}
