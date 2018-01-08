package org.aurinkopg.postgresql;

import org.aurinkopg.GlobalConstants;
import org.aurinkopg.Snapshot;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.jdbc.PgStatement;
import org.postgresql.util.HostSpec;

import javax.sql.DataSource;
import java.sql.Connection;
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

    private final ConnectionInfo connectionInfo;
    private DataSource dataSource;
    private PgConnection pgConnection;

    /**
     * Not meant to be instantiated via constructor.
     * Use factory method {@link #connect(ConnectionInfo)}.
     *
     * @param connectionInfo connection info.
     */
    private Database(ConnectionInfo connectionInfo) throws SQLException {
        this.connectionInfo = connectionInfo;
        this.pgConnection = openPgConnection();
    }

    /**
     * Not meant to be instantiated via constructor.
     * Use factory method {@link #connect(ConnectionInfo, DataSource)}.
     *
     * @param connectionInfo connection info.
     * @param dataSource     a DataSource which provides connections to a PostgreSQL database.
     *                       This does not work with other databases.
     */
    private Database(ConnectionInfo connectionInfo, DataSource dataSource) throws SQLException {
        this.connectionInfo = connectionInfo;
        this.dataSource = dataSource;
        this.pgConnection = openPgConnection();
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

    public static Database connect(ConnectionInfo connectionInfo, DataSource dataSource) throws SQLException {
        Objects.requireNonNull(connectionInfo, "Parameter connectionInfo in Database.connect(connectionInfo, dataSource) cannot be null!");
        Objects.requireNonNull(dataSource, "Parameter dataSource in Database.connect(connectionInfo, dataSource) cannot be null!");
        Connection connection = dataSource.getConnection();
        if (!(connection instanceof PgConnection)) {
            throw new IllegalStateException(
                "Aurinko-pg only supports PostgreSQL and its class PgConnection. " +
                    "The type of connection obtained from the DataSource was: " +
                    connection.getClass() + ", which is not the correct type. " +
                    "Maybe the DataSource was configured for a different database than PostgreSQL?");
        }
        return new Database(connectionInfo, dataSource);
    }

    public Snapshot takeSnapshot(String snapshotName) throws SQLException {
        Snapshot snapshot = new Snapshot(snapshotName);
        killAllOtherConnectionsToDatabase(connectionInfo.getDatabase());
        copyDatabase(connectionInfo.getDatabase(), snapshot.getName());
        return snapshot;
    }

    public void restoreSnapshot(Snapshot snapshot) throws Exception {
        // TODO: How to "drop" the current database and copy the data from the snapshot into it?
        // First, we close the current connection and open another connection into the snapshot db.
        // Store the main database name first.
        ConnectionInfo originalConnectionInfo = connectionInfo;
        ConnectionInfo snapshotConnectionInfo =
            ConnectionInfo.Builder.from(connectionInfo).setDatabase(snapshot.getName()).build();
        // TODO How to open a connection from the DataSource to a different database?
        // this.pgConnection = openPgConnection(snapshotConnectionInfo);
        this.pgConnection = openPgConnection();
        dropDatabase(originalConnectionInfo.getDatabase());
        killAllOtherConnectionsToDatabase(snapshot.getName());
        copyDatabase(snapshot.getName(), originalConnectionInfo.getDatabase());
        // TODO How to open a connection from the DataSource to a different database?
        // this.pgConnection = openPgConnection(originalConnectionInfo);
        this.pgConnection = openPgConnection();
    }

    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        dropDatabase(snapshot.getName());
    }

    public Connection getConnection() {
        return pgConnection;
    }

    @Override
    public void close() throws Exception {
        pgConnection.close();
    }

    private void dropDatabase(String databaseName) throws SQLException {
        killAllOtherConnectionsToDatabase(databaseName);
        String sql = String.format(DROP_DATABASE_SQL, databaseName);
        pgConnection.setAutoCommit(true);
        pgConnection.execSQLUpdate(sql);
        pgConnection.setAutoCommit(false);
    }

    private void copyDatabase(String from, String to) throws SQLException {
        if (doesDatabaseExist(to)) {
            dropDatabase(to);
        }
        pgConnection.setAutoCommit(false);
        killAllOtherConnectionsToDatabase(from);
        pgConnection.setAutoCommit(true);
        String sql = String.format(
            COPY_DATABASE_SQL,
            to,
            from,
            connectionInfo.getPgUsername());
        pgConnection.execSQLUpdate(sql);
        pgConnection.setAutoCommit(false);
        pgConnection.commit();
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

    // TODO: This method should be parameterized by the database which we want to connect to.
    private PgConnection openPgConnection() throws SQLException {
        if (dataSource != null) {
            Connection connection =
                dataSource.getConnection(
                    connectionInfo.getPgUsername(),
                    connectionInfo.getPgPassword());
            if (!(connection instanceof PgConnection)) {
                throw new IllegalStateException(
                    "Aurinko-pg only supports PostgreSQL and its class PgConnection. " +
                        "The type of connection obtained from the DataSource was: " +
                        connection.getClass() + ", which is not the correct type. " +
                        "Maybe the DataSource was configured for a different database than PostgreSQL?");
            } else {
                this.pgConnection = (PgConnection) connection;
                return this.pgConnection;
            }
        } else {
            HostSpec hostSpec = new HostSpec(connectionInfo.getHost(), connectionInfo.getPort());
            HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
            Properties info = new Properties();
            info.putAll(connectionInfo.getConnectionProperties());
            PGProperty.PASSWORD.set(info, connectionInfo.getPgPassword());
            // TODO Add application version here
            PGProperty.APPLICATION_NAME.set(info, GlobalConstants.APPLICATION_NAME);
            PgConnection pgConnection = new PgConnection(
                hostSpecs,
                connectionInfo.getPgUsername(),
                connectionInfo.getDatabase(),
                info,
                connectionInfo.getJdbcUrl());
            pgConnection.setAutoCommit(false);
            return this.pgConnection;
        }
    }
}
