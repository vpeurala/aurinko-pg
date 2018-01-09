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
        killAllOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), connection);
        copyDatabase(originalConnectionInfo.getDatabase(), snapshot.getName(), connection);
        return snapshot;
    }

    @Override
    public void restoreSnapshot(Snapshot snapshot) throws Exception {
        // First, we close the current connection and open another connection into the snapshot db.
        // Store the main database name first.
        PgConnection connection = openPgConnection(connectionInfoForSnapshot(snapshot));
        dropDatabase(originalConnectionInfo.getDatabase(), connection);
        killAllOtherConnectionsToDatabase(snapshot.getName(), connection);
        copyDatabase(snapshot.getName(), originalConnectionInfo.getDatabase(), connection);
    }

    @Override
    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        dropDatabase(snapshot.getName(), openPgConnection(connectionInfoForSnapshot(snapshot)));
    }

    public PgConnection getConnection() throws SQLException {
        return openPgConnection(originalConnectionInfo);
    }

    private void dropDatabase(String databaseName, PgConnection connection) throws SQLException {
        killAllOtherConnectionsToDatabase(databaseName, connection);
        String sql = String.format(DROP_DATABASE_SQL, databaseName);
        connection.setAutoCommit(true);
        connection.execSQLUpdate(sql);
        connection.setAutoCommit(false);
    }

    private void copyDatabase(String from, String to, PgConnection connection) throws SQLException {
        if (doesDatabaseExist(to, connection)) {
            dropDatabase(to, connection);
        }
        connection.setAutoCommit(false);
        killAllOtherConnectionsToDatabase(from, connection);
        connection.setAutoCommit(true);
        String sql = String.format(
            COPY_DATABASE_SQL,
            to,
            from,
            originalConnectionInfo.getPgUsername());
        connection.execSQLUpdate(sql);
        connection.setAutoCommit(false);
        connection.commit();
    }

    /**
     * TODO: UPDATE pg_database SET datallowconn = 'false' WHERE datname = 'mydb';
     * https://dba.stackexchange.com/questions/11893/force-drop-db-while-others-may-be-connected
     *
     * @param databaseName
     * @throws SQLException
     */
    private void killAllOtherConnectionsToDatabase(String databaseName, PgConnection connection) throws SQLException {
        String sql = String.format(
            KILL_ALL_OTHER_CONNECTIONS_SQL,
            databaseName);
        PgStatement pgStatement = (PgStatement) connection.createStatement(
            TYPE_FORWARD_ONLY,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT);
        pgStatement.executeWithFlags(sql, QUERY_NO_RESULTS);
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
        pgConnection.setAutoCommit(false);
        return pgConnection;
    }
}
