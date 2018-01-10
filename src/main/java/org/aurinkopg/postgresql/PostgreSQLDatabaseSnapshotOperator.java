package org.aurinkopg.postgresql;

import org.aurinkopg.GlobalConstants;
import org.aurinkopg.Snapshot;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.util.HostSpec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static java.sql.ResultSet.*;
import static org.postgresql.core.QueryExecutor.QUERY_NO_RESULTS;

/**
 * Contains PostgreSQL database operations.
 */
class PostgreSQLDatabaseSnapshotOperator implements PostgreSQLDatabase {
    private static final String ALLOW_NEW_CONNECTIONS_FOR_DATABASE_SQL = "UPDATE pg_database " +
        "SET datallowconn = 'true' " +
        "WHERE datname = '%s'";
    private static final String ALLOW_NEW_CONNECTIONS_FOR_ALL_DATABASES_SQL = "UPDATE pg_database " +
        "SET datallowconn = 'true'";
    private static final String BLOCK_NEW_CONNECTIONS_SQL = "UPDATE pg_database " +
        "SET datallowconn = 'false' " +
        "WHERE datname = '%s'";
    private static final String DOES_DATABASE_ALLOW_NEW_CONNECTIONS_SQL = "SELECT " +
        "datallowconn " +
        "FROM pg_database " +
        "WHERE datname = '%s'";
    private static final String COPY_DATABASE_SQL = "CREATE DATABASE %s WITH TEMPLATE '%s' OWNER '%s'";
    private static final String DOES_DATABASE_EXIST_SQL = "SELECT 1 FROM pg_database WHERE datname = '%s'";
    private static final String DROP_DATABASE_SQL = "DROP DATABASE %s";
    private static final String KILL_ALL_OTHER_CONNECTIONS_SQL = "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
        "FROM pg_stat_activity " +
        "WHERE pg_stat_activity.datname = '%s' " +
        "AND pid <> pg_backend_pid()";

    private final ConnectionInfo originalConnectionInfo;

    /**
     * Not meant to be instantiated via constructor.
     * Use factory method {@link #connect(ConnectionInfo)}.
     *
     * @param connectionInfo connection info.
     */
    PostgreSQLDatabaseSnapshotOperator(ConnectionInfo connectionInfo) throws SQLException {
        this.originalConnectionInfo = connectionInfo;
        // Check that the connection works.
        openPgConnection(this.originalConnectionInfo);
    }

    @Override
    public Snapshot takeSnapshot(String snapshotName) throws SQLException {
        PgConnection connection = openPgConnection(originalConnectionInfo);
        Snapshot snapshot = new Snapshot(snapshotName);
        blockNewConnectionsToDatabase(originalConnectionInfo.getDatabase(), connection);
        killOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), connection);
        copyDatabase(originalConnectionInfo.getDatabase(), snapshot.getName(), connection);
        allowNewConnectionsToAllDatabases(connection);
        return snapshot;
    }

    @Override
    public void restoreSnapshot(Snapshot snapshot) throws Exception {
        PgConnection mainDbConnection = openPgConnection(originalConnectionInfo);
        blockNewConnectionsToDatabase(originalConnectionInfo.getDatabase(), mainDbConnection);
        killOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), mainDbConnection);
        mainDbConnection.close();
        PgConnection snapshotDbConnection = openPgConnection(connectionInfoForSnapshot(snapshot));
        dropDatabase(originalConnectionInfo.getDatabase(), snapshotDbConnection);
        blockNewConnectionsToDatabase(snapshot.getName(), snapshotDbConnection);
        killOtherConnectionsToDatabase(snapshot.getName(), snapshotDbConnection);
        copyDatabase(snapshot.getName(), originalConnectionInfo.getDatabase(), snapshotDbConnection);
        allowNewConnectionsToDatabase(originalConnectionInfo.getDatabase(), snapshotDbConnection);
        snapshotDbConnection.close();
        mainDbConnection = openPgConnection(originalConnectionInfo);
        allowNewConnectionsToAllDatabases(mainDbConnection);
    }

    @Override
    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        dropDatabase(snapshot.getName(), openPgConnection(originalConnectionInfo));
        allowNewConnectionsToAllDatabases(openPgConnection(originalConnectionInfo));
    }

    public PgConnection getConnection() throws SQLException {
        return openPgConnection(originalConnectionInfo);
    }

    private void dropDatabase(String databaseName, PgConnection connection) throws SQLException {
        blockNewConnectionsToDatabase(databaseName, connection);
        killOtherConnectionsToDatabase(databaseName, connection);
        String sql = String.format(DROP_DATABASE_SQL, databaseName);
        connection.execSQLUpdate(sql);
        allowNewConnectionsToAllDatabases(connection);
    }

    private void copyDatabase(String from, String to, PgConnection connection) throws SQLException {
        if (doesDatabaseExist(to, connection)) {
            dropDatabase(to, connection);
        }
        blockNewConnectionsToDatabase(from, connection);
        killOtherConnectionsToDatabase(from, connection);
        connection.execSQLUpdate(String.format(
            COPY_DATABASE_SQL,
            to,
            from,
            originalConnectionInfo.getPgUsername()));
        allowNewConnectionsToAllDatabases(connection);
    }

    private void allowNewConnectionsToDatabase(String databaseName, PgConnection connection) throws SQLException {
        String allowSql = String.format(ALLOW_NEW_CONNECTIONS_FOR_DATABASE_SQL, databaseName);
        createStatement(connection).executeWithFlags(allowSql, QUERY_NO_RESULTS);
    }

    private void allowNewConnectionsToAllDatabases(PgConnection connection) throws SQLException {
        createStatement(connection).executeWithFlags(ALLOW_NEW_CONNECTIONS_FOR_ALL_DATABASES_SQL, QUERY_NO_RESULTS);
    }

    private void blockNewConnectionsToDatabase(String databaseName, PgConnection connection) throws SQLException {
        String blockSql = String.format(BLOCK_NEW_CONNECTIONS_SQL, databaseName);
        createStatement(connection).executeWithFlags(blockSql, QUERY_NO_RESULTS);
    }

    private void killOtherConnectionsToDatabase(String databaseName, PgConnection connection) throws SQLException {
        String killSql = String.format(KILL_ALL_OTHER_CONNECTIONS_SQL, databaseName);
        createStatement(connection).executeWithFlags(killSql, QUERY_NO_RESULTS);
    }

    private PgStatement createStatement(PgConnection connection) throws SQLException {
        return (PgStatement) connection.createStatement(
            TYPE_FORWARD_ONLY,
            CONCUR_UPDATABLE,
            CLOSE_CURSORS_AT_COMMIT);
    }

    private boolean doesDatabaseExist(String databaseName, PgConnection connection) throws SQLException {
        String sql = String.format(
            DOES_DATABASE_EXIST_SQL,
            databaseName
        );
        ResultSet resultSet = connection.execSQLQuery(sql, TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        return resultSet.next();
    }

    private ConnectionInfo connectionInfoForSnapshot(Snapshot snapshot) {
        return ConnectionInfo.Builder.
            from(originalConnectionInfo).
            setDatabase(snapshot.getName()).
            build();
    }

    private boolean doesDatabaseAllowNewConnections(String databaseName, PgConnection connection) throws SQLException {
        String sql = String.format(DOES_DATABASE_ALLOW_NEW_CONNECTIONS_SQL, databaseName);
        ResultSet resultSet = connection.execSQLQuery(sql, TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        resultSet.next();
        String allow = resultSet.getString(1);
        switch (allow) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new IllegalStateException(
                    "Result from doesDatabaseAllowNewConnections was unknown: " + allow);
        }
    }

    private PgConnection openPgConnection(ConnectionInfo localConnectionInfo) throws SQLException {
        HostSpec hostSpec = new HostSpec(localConnectionInfo.getHost(), localConnectionInfo.getPort());
        HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
        Properties info = new Properties();
        info.putAll(localConnectionInfo.getConnectionProperties());
        PGProperty.PASSWORD.set(info, localConnectionInfo.getPgPassword());
        PGProperty.APPLICATION_NAME.set(info,
            String.format("%s-%s",
                GlobalConstants.LIBRARY_NAME,
                GlobalConstants.LIBRARY_VERSION));
        PgConnection pgConnection = new PgConnection(
            hostSpecs,
            localConnectionInfo.getPgUsername(),
            localConnectionInfo.getDatabase(),
            info,
            localConnectionInfo.getJdbcUrl());
        allowNewConnectionsToDatabase(localConnectionInfo.getDatabase(), pgConnection);
        // Note: Sql commands which affect a whole database (CREATE DATABASE,
        // COPY DATABASE and DROP DATABASE) cannot be run without auto-commit.
        // Thus we must set auto-commit to true for these connections which
        // execute whole-database operations.
        // Do not use these connections for ordinary JDBC queries and updates.
        // You should not normally use auto-commit in production applications.
        pgConnection.setAutoCommit(true);
        return pgConnection;
    }
}
