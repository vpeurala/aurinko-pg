package org.aurinkopg.postgresql;

import org.aurinkopg.Snapshot;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.HostSpec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Contains PostgreSQL database operations.
 */
public class Database {
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
        pgConnection.setAutoCommit(false);
        return new Database(connectionInfo, pgConnection);
    }

    public Snapshot takeSnapshot() throws SQLException {
        final String SNAPSHOT_NAME = "snapshot-1";
        try {
            pgConnection.execSQLUpdate(
                String.format("CREATE DATABASE %s WITH TEMPLATE %s OWNER %s;",
                    SNAPSHOT_NAME,
                    connectionInfo.getDatabase(),
                    connectionInfo.getPgUsername())
            );
            pgConnection.commit();
            return new Snapshot("snapshot");
        } catch (SQLException e) {
            pgConnection.rollback();
            throw e;
        }
    }

    public void restoreSnapshot(Snapshot snapshot) throws SQLException {
        try {
            pgConnection.execSQLUpdate(
                String.format("CREATE DATABASE %s WITH TEMPLATE %s OWNER %s;",
                    connectionInfo.getDatabase(),
                    snapshot.getName(),
                    connectionInfo.getPgUsername())
            );
            pgConnection.commit();
        } catch (SQLException e) {
            pgConnection.rollback();
            throw e;
        }
    }

    public Connection getConnection() {
        return pgConnection;
    }
}
