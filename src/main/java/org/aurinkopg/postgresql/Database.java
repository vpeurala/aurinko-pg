package org.aurinkopg.postgresql;

import org.aurinkopg.Snapshot;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.util.HostSpec;

import java.nio.charset.Charset;
import java.sql.Connection;
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
        validateSnapshotName(snapshotName);
        killAllOtherConnectionsToDatabase();
        copyDatabaseToSnapshot(snapshotName, connectionInfo.getDatabase());
        return new Snapshot(snapshotName);
    }

    public void restoreSnapshot(Snapshot snapshot) throws SQLException {
        killAllOtherConnectionsToDatabase();
        copyDatabaseToSnapshot(connectionInfo.getDatabase(), snapshot.getName());
    }

    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        String sql = String.format(DROP_DATABASE_SQL, snapshot.getName());
        // TODO Remove debug logging
        System.out.println("sql:\n" + sql + "\n");
        pgConnection.execSQLUpdate(sql);
    }

    public Connection getConnection() {
        return pgConnection;
    }

    /**
     * See https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS.
     *
     * @param snapshotName a name you would like to use as a snapshot DB name.
     * @throws SQLException if the name does not conform to the rules.
     */
    private void validateSnapshotName(String snapshotName) throws SQLException {
        int lengthInBytes = snapshotName.getBytes(Charset.forName("UTF-8")).length;
        if (lengthInBytes > 63) {
            throw new SQLException(
                "Your snapshot name is too long. " +
                    "The maximum length is 63 bytes. " +
                    "You tried to use '" +
                    snapshotName +
                    "', which is " +
                    lengthInBytes +
                    " bytes long.");
        }
        if (snapshotName.isEmpty()) {
            throw new SQLException("You tried to use an empty string as a snapshot name.");
        }
        if (!snapshotName.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new SQLException("Your snapshot name '" +
                snapshotName +
                "'did not match the pattern of acceptable database names. " +
                "See https://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS for the rules.");
        }
    }

    private void copyDatabaseToSnapshot(String snapshotName, String database) throws SQLException {
        String sql = String.format(
            COPY_DATABASE_SQL,
            snapshotName,
            database,
            connectionInfo.getPgUsername());
        // TODO Remove debug logging
        System.out.println("sql:\n" + sql + "\n");
        pgConnection.execSQLUpdate(sql);
    }

    private void killAllOtherConnectionsToDatabase() throws SQLException {
        String sql = String.format(
            KILL_ALL_OTHER_CONNECTIONS_SQL,
            connectionInfo.getDatabase());
        // TODO Remove debug logging
        System.out.println("sql:\n" + sql + "\n");
        PgStatement pgStatement = (PgStatement) pgConnection.createStatement(
            TYPE_FORWARD_ONLY,
            CONCUR_READ_ONLY,
            CLOSE_CURSORS_AT_COMMIT);
        pgStatement.executeWithFlags(sql, QUERY_NO_RESULTS);
    }
}
