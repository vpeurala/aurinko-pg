package org.aurinkopg.postgresql;

import org.aurinkopg.Snapshot;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.util.HostSpec;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static java.sql.ResultSet.*;
import static org.postgresql.core.QueryExecutor.QUERY_NO_RESULTS;

/**
 * Contains PostgreSQL database operations.
 */
public class Database {
    private static final String COPY_DATABASE_SQL = "CREATE DATABASE %s WITH TEMPLATE '%s' OWNER '%s'";
    private static final String DOES_DATABASE_EXIST_SQL = "select 1 from pg_database where datname = '%s'";
    private static final String DROP_DATABASE_SQL = "DROP DATABASE %s";
    private static final String KILL_ALL_OTHER_CONNECTIONS_SQL = "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
        "FROM pg_stat_activity " +
        "WHERE pg_stat_activity.datname = '%s' " +
        "AND pid <> pg_backend_pid()";

    private final ConnectionInfo connectionInfo;
    private final PgConnection pgConnection;

    /**
     * Not meant to be instantiated via constructor.
     * Use factory method {@link #connect(ConnectionInfo)}.
     *
     * @param pgConnection a database connection.
     */
    private Database(ConnectionInfo connectionInfo, PgConnection pgConnection) {
        this.connectionInfo = connectionInfo;
        this.pgConnection = pgConnection;
    }

    /**
     * Create a database connection, returning a Database object
     * on which you can execute operations.
     *
     * @param connectionInfo an object which holds all the data needed to establish the connection.
     * @return a Database object.
     * @throws SQLException if obtaining a connection fails for any reason.
     */
    public static Database connect(ConnectionInfo connectionInfo) throws SQLException {
        Objects.requireNonNull(connectionInfo, "Parameter connectionInfo in Database.connect(connectionInfo) cannot be null!");
        HostSpec hostSpec = new HostSpec(connectionInfo.getHost(), connectionInfo.getPort());
        HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
        Properties info = new Properties();
        info.putAll(connectionInfo.getConnectionProperties());
        PGProperty.PASSWORD.set(info, connectionInfo.getPgPassword());
        PgConnection pgConnection = new PgConnection(
            hostSpecs,
            connectionInfo.getPgUsername(),
            connectionInfo.getDatabase(),
            info,
            connectionInfo.getJdbcUrl());
        // Note: CREATE DATABASE needs to be run with autocommit on. It does not work without it.
        pgConnection.setAutoCommit(true);
        return new Database(connectionInfo, pgConnection);
    }

    public Snapshot takeSnapshot(String snapshotName) throws SQLException {
        Snapshot snapshot = new Snapshot(snapshotName);
        killAllOtherConnectionsToDatabase(connectionInfo.getDatabase());
        copyDatabaseToSnapshot(snapshot.getName(), connectionInfo.getDatabase());
        return snapshot;
    }

    public void restoreSnapshot(Snapshot snapshot) throws SQLException {
        // TODO: How to "drop" the current database and copy the data from the snapshot into it?
        dropDatabase(connectionInfo.getDatabase());
        killAllOtherConnectionsToDatabase(snapshot.getName());
        copyDatabaseToSnapshot(connectionInfo.getDatabase(), snapshot.getName());
    }

    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        dropDatabase(snapshot.getName());
    }

    public Connection getConnection() {
        return pgConnection;
    }

    private void dropDatabase(String databaseName) throws SQLException {
        killAllOtherConnectionsToDatabase(databaseName);
        String sql = String.format(DROP_DATABASE_SQL, databaseName);
        pgConnection.execSQLUpdate(sql);
    }

    private void copyDatabaseToSnapshot(String snapshotName, String database) throws SQLException {
        if (doesDatabaseExist(snapshotName)) {
            dropDatabase(snapshotName);
        }
        killAllOtherConnectionsToDatabase(database);
        String sql = String.format(
            COPY_DATABASE_SQL,
            snapshotName,
            database,
            connectionInfo.getPgUsername());
        pgConnection.execSQLUpdate(sql);
    }

    private void killAllOtherConnectionsToDatabase(String databaseName) throws SQLException {
        String sql = String.format(
            KILL_ALL_OTHER_CONNECTIONS_SQL,
            databaseName);
        PgStatement pgStatement = (PgStatement) pgConnection.createStatement(
            TYPE_FORWARD_ONLY,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT);
        pgStatement.executeWithFlags(sql, QUERY_NO_RESULTS);
    }

    private boolean doesDatabaseExist(String databaseName) throws SQLException {
        String sql = String.format(
            DOES_DATABASE_EXIST_SQL,
            databaseName
        );
        ResultSet resultSet = pgConnection.execSQLQuery(sql, TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        return resultSet.next();
    }
}
