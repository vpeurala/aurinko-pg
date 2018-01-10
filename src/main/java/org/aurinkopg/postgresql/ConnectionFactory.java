package org.aurinkopg.postgresql;

import org.aurinkopg.GlobalConstants;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.HostSpec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

class ConnectionFactory {
    static Connection openConnection(ConnectionInfo connectionInfo) throws SQLException {
        HostSpec hostSpec = new HostSpec(connectionInfo.getHost(), connectionInfo.getPort());
        HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
        Properties info = new Properties();
        info.putAll(connectionInfo.getConnectionProperties());

        PGProperty.APPLICATION_NAME.set(info,
            String.format("%s-%s",
                GlobalConstants.LIBRARY_NAME,
                GlobalConstants.LIBRARY_VERSION));
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(info, "9.2");
        // CONNECT_TIMEOUT is specified in seconds. Thus, this means 3 seconds.
        PGProperty.CONNECT_TIMEOUT.set(info, 3);
        PGProperty.LOAD_BALANCE_HOSTS.set(info, false);
        PGProperty.LOG_UNCLOSED_CONNECTIONS.set(info, true);
        PGProperty.LOGGER_LEVEL.set(info, "TRACE");
        // LOGIN_TIMEOUT is specified in seconds. Thus, this means 3 seconds.
        PGProperty.LOGIN_TIMEOUT.set(info, 3);
        PGProperty.PASSWORD.set(info, connectionInfo.getPgPassword());
        PGProperty.PREFER_QUERY_MODE.set(info, "simple");
        // SOCKET_TIMEOUT is specified in seconds. Thus, this means 3 seconds.
        PGProperty.SOCKET_TIMEOUT.set(info, 3);
        PGProperty.TARGET_SERVER_TYPE.set(info, "master");

        Connection connection = new PgConnection(
            hostSpecs,
            connectionInfo.getPgUsername(),
            connectionInfo.getDatabase(),
            info,
            connectionInfo.getJdbcUrl());

        // Note: Sql commands which affect a whole database (CREATE DATABASE,
        // COPY DATABASE and DROP DATABASE) cannot be run without auto-commit.
        // Thus we must set auto-commit to true for these connections which
        // execute whole-database operations.
        // Do not use these connections for ordinary application-level JDBC queries and updates.
        // Use a {@link javax.sql.DataSource} for them.
        // This is a special tool, intended only for testing.
        connection.setAutoCommit(true);
        return connection;
    }
}
