package org.aurinkopg.postgresql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

    private static List<Map<String, Object>> resultSetToListOfMaps(ResultSet resultSet) {
        return new ArrayList<>();
    }
}
