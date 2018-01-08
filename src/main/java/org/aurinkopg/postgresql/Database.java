package org.aurinkopg.postgresql;

import org.aurinkopg.GlobalConstants;
import org.aurinkopg.Snapshot;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.util.HostSpec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static java.sql.ResultSet.*;
import static org.postgresql.core.QueryExecutor.QUERY_NO_RESULTS;

/**
 * Contains PostgreSQL database operations.
 * <p>
 * TODO Remove there psql helpers when done:
 * LIST DATABASES: select * from pg_database;
 * KILL ALL CONNECTIONS: SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'jaanmurtaja' AND pid <> pg_backend_pid();
 * SELECT DATASET: SELECT laiva.id AS laiva_id, laiva.nimi AS laiva_nimi, CAST(EXTRACT(YEAR FROM laiva.valmistumisvuosi) AS INT) AS laiva_valmistumisvuosi, laiva.akseliteho AS laiva_akseliteho, laiva.vetoisuus AS laiva_vetoisuus, laiva.pituus AS laiva_pituus, laiva.leveys AS laiva_leveys ,valtio.id AS valtio_id, valtio.nimi AS valtio_nimi FROM laiva INNER JOIN valtio ON laiva.omistaja = valtio.id ORDER BY laiva_id;
 */
public class Database implements AutoCloseable {
    private static final String COPY_DATABASE_SQL = "CREATE DATABASE %s WITH TEMPLATE '%s' OWNER '%s'";
    private static final String DOES_DATABASE_EXIST_SQL = "select 1 from pg_database where datname = '%s'";
    private static final String DROP_DATABASE_SQL = "DROP DATABASE %s";
    private static final String KILL_ALL_OTHER_CONNECTIONS_SQL = "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
        "FROM pg_stat_activity " +
        "WHERE pg_stat_activity.datname = '%s' " +
        "AND pid <> pg_backend_pid()";

    private final ConnectionInfo originalConnectionInfo;
    private PgConnection connection;

    /**
     * Not meant to be instantiated via constructor.
     * Use factory method {@link #connect(ConnectionInfo)}.
     *
     * @param connectionInfo connection info.
     */
    private Database(ConnectionInfo connectionInfo) throws SQLException {
        this.originalConnectionInfo = connectionInfo;
        this.connection = openPgConnection(this.originalConnectionInfo);
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
        return new Database(connectionInfo);
    }

    public Snapshot takeSnapshot(String snapshotName) throws SQLException {
        this.connection = openPgConnection(originalConnectionInfo);
        Snapshot snapshot = new Snapshot(snapshotName);
        killAllOtherConnectionsToDatabase(originalConnectionInfo.getDatabase());
        copyDatabase(originalConnectionInfo.getDatabase(), snapshot.getName());
        return snapshot;
    }

    public void restoreSnapshot(Snapshot snapshot) throws Exception {
        // First, we close the current connection and open another connection into the snapshot db.
        // Store the main database name first.
        ConnectionInfo originalConnectionInfo = this.originalConnectionInfo;
        this.connection = openPgConnection(connectionInfoForSnapshot(snapshot));
        dropDatabase(originalConnectionInfo.getDatabase());
        killAllOtherConnectionsToDatabase(snapshot.getName());
        copyDatabase(snapshot.getName(), originalConnectionInfo.getDatabase());
        this.connection = openPgConnection(originalConnectionInfo);
    }

    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        dropDatabase(snapshot.getName());
    }

    public PgConnection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private void dropDatabase(String databaseName) throws SQLException {
        killAllOtherConnectionsToDatabase(databaseName);
        String sql = String.format(DROP_DATABASE_SQL, databaseName);
        connection.setAutoCommit(true);
        connection.execSQLUpdate(sql);
        connection.setAutoCommit(false);
    }

    private void copyDatabase(String from, String to) throws SQLException {
        if (doesDatabaseExist(to)) {
            dropDatabase(to);
        }
        connection.setAutoCommit(false);
        killAllOtherConnectionsToDatabase(from);
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

    private void killAllOtherConnectionsToDatabase(String databaseName) throws SQLException {
        String sql = String.format(
            KILL_ALL_OTHER_CONNECTIONS_SQL,
            databaseName);
        PgStatement pgStatement = (PgStatement) connection.createStatement(
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
        // TODO Add application version here
        PGProperty.APPLICATION_NAME.set(info, GlobalConstants.APPLICATION_NAME);
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
