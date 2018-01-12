package org.aurinkopg.postgresql;

import org.aurinkopg.Snapshot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.aurinkopg.postgresql.SqlExecutor.executeSqlQuery;
import static org.aurinkopg.postgresql.SqlExecutor.executeSqlUpdate;

/**
 * Contains PostgreSQL database operations.
 */
class PostgreSQLDatabaseSnapshotOperator implements DatabaseSnapshotOperator {
    private static final String ALLOW_NEW_CONNECTIONS_FOR_DATABASE_SQL = "UPDATE pg_database " +
        "SET datallowconn = 'true' " +
        "WHERE datname = '%s'";
    private static final String ALLOW_NEW_CONNECTIONS_FOR_ALL_DATABASES_SQL = "UPDATE pg_database " +
        "SET datallowconn = 'true'";
    private static final String BLOCK_NEW_CONNECTIONS_SQL = "UPDATE pg_database " +
        "SET datallowconn = 'false' " +
        "WHERE datname = '%s'";
    private static final String CANCEL_ALL_OTHER_CONNECTIONS_SQL = "SELECT pg_cancel_backend(pg_stat_activity.pid) " +
        "FROM pg_stat_activity " +
        "WHERE pg_stat_activity.datname = '%s' " +
        "AND pid <> pg_backend_pid()";
    private static final String DOES_DATABASE_ALLOW_NEW_CONNECTIONS_SQL = "SELECT " +
        "datallowconn " +
        "FROM pg_database " +
        "WHERE datname = '%s'";
    private static final String COPY_DATABASE_SQL = "CREATE DATABASE %s WITH TEMPLATE '%s' OWNER '%s'";
    private static final String DOES_DATABASE_EXIST_SQL = "SELECT 1 FROM pg_database WHERE datname = '%s'";
    private static final String DROP_DATABASE_SQL = "DROP DATABASE %s";
    private static final String TERMINATE_ALL_OTHER_CONNECTIONS_SQL = "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
        "FROM pg_stat_activity " +
        "WHERE pg_stat_activity.datname = '%s' " +
        "AND pid <> pg_backend_pid()";

    private final ConnectionInfo originalConnectionInfo;

    /**
     * Not meant to be instantiated via constructor.
     * Use factory method {@link DatabaseSnapshotOperator#create(ConnectionInfo)}.
     *
     * @param connectionInfo connection info.
     */
    PostgreSQLDatabaseSnapshotOperator(ConnectionInfo connectionInfo) throws SQLException {
        Objects.requireNonNull(connectionInfo, "Parameter connectionInfo in Database.connect(connectionInfo) cannot be null!");
        this.originalConnectionInfo = connectionInfo;
        // Check that the connection works and the user has superuser privileges.
        Connection testConnection = ConnectionFactory.openConnection(this.originalConnectionInfo);
        checkSuperuser(connectionInfo.getPgUsername(), testConnection);
        testConnection.close();
    }

    @Override
    public Snapshot takeSnapshot(String snapshotName) throws SQLException {
        Objects.requireNonNull(snapshotName, "Parameter snapshotName in Database.takeSnapshot(connectionInfo) cannot be null!");
        try (Connection connection = ConnectionFactory.openConnection(originalConnectionInfo)) {
            Snapshot snapshot = new Snapshot(snapshotName);
            blockNewConnectionsToDatabase(originalConnectionInfo.getDatabase(), connection);
            cancelOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), connection);
            terminateOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), connection);
            copyDatabase(originalConnectionInfo.getDatabase(), snapshot.getName(), connection);
            allowNewConnectionsToAllDatabases(connection);
            return snapshot;
        }
    }

    // TODO This method is too complex and difficult to understand.
    @Override
    public void restoreSnapshot(Snapshot snapshot) throws Exception {
        Objects.requireNonNull(snapshot, "Parameter snapshot in Database.restoreSnapshot(connectionInfo) cannot be null!");
        Connection mainDbConnection = ConnectionFactory.openConnection(originalConnectionInfo);
        blockNewConnectionsToDatabase(originalConnectionInfo.getDatabase(), mainDbConnection);
        cancelOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), mainDbConnection);
        terminateOtherConnectionsToDatabase(originalConnectionInfo.getDatabase(), mainDbConnection);
        mainDbConnection.close();
        Connection snapshotDbConnection = ConnectionFactory.openConnection(connectionInfoForSnapshot(snapshot));
        dropDatabase(originalConnectionInfo.getDatabase(), snapshotDbConnection);
        blockNewConnectionsToDatabase(snapshot.getName(), snapshotDbConnection);
        cancelOtherConnectionsToDatabase(snapshot.getName(), snapshotDbConnection);
        terminateOtherConnectionsToDatabase(snapshot.getName(), snapshotDbConnection);
        copyDatabase(snapshot.getName(), originalConnectionInfo.getDatabase(), snapshotDbConnection);
        allowNewConnectionsToDatabase(originalConnectionInfo.getDatabase(), snapshotDbConnection);
        snapshotDbConnection.close();
        mainDbConnection = ConnectionFactory.openConnection(originalConnectionInfo);
        allowNewConnectionsToAllDatabases(mainDbConnection);
        mainDbConnection.close();
    }

    @Override
    public void deleteSnapshot(Snapshot snapshot) throws SQLException {
        Objects.requireNonNull(snapshot, "Parameter snapshot in Database.deleteSnapshot(connectionInfo) cannot be null!");
        dropDatabase(snapshot.getName(), ConnectionFactory.openConnection(originalConnectionInfo));
        allowNewConnectionsToAllDatabases(ConnectionFactory.openConnection(originalConnectionInfo));
    }

    @Override
    public Connection openConnection() throws SQLException {
        return ConnectionFactory.openConnection(originalConnectionInfo);
    }

    private void dropDatabase(String databaseName, Connection connection) throws SQLException {
        blockNewConnectionsToDatabase(databaseName, connection);
        cancelOtherConnectionsToDatabase(databaseName, connection);
        terminateOtherConnectionsToDatabase(databaseName, connection);
        String sql = String.format(DROP_DATABASE_SQL, databaseName);
        executeSqlUpdate(sql, connection);
        allowNewConnectionsToAllDatabases(connection);
    }

    private void copyDatabase(String from, String to, Connection connection) throws SQLException {
        if (doesDatabaseExist(to, connection)) {
            dropDatabase(to, connection);
        }
        blockNewConnectionsToDatabase(from, connection);
        cancelOtherConnectionsToDatabase(from, connection);
        terminateOtherConnectionsToDatabase(from, connection);
        String sql = String.format(
            COPY_DATABASE_SQL,
            to,
            from,
            originalConnectionInfo.getPgUsername());
        executeSqlUpdate(sql, connection);
        allowNewConnectionsToAllDatabases(connection);
    }

    private void allowNewConnectionsToDatabase(String databaseName, Connection connection) throws SQLException {
        String sql = String.format(ALLOW_NEW_CONNECTIONS_FOR_DATABASE_SQL, databaseName);
        executeSqlUpdate(sql, connection);
    }

    private void allowNewConnectionsToAllDatabases(Connection connection) throws SQLException {
        executeSqlUpdate(ALLOW_NEW_CONNECTIONS_FOR_ALL_DATABASES_SQL, connection);
    }

    private void blockNewConnectionsToDatabase(String databaseName, Connection connection) throws SQLException {
        String sql = String.format(BLOCK_NEW_CONNECTIONS_SQL, databaseName);
        executeSqlUpdate(sql, connection);
    }

    private void cancelOtherConnectionsToDatabase(String databaseName, Connection connection) throws SQLException {
        String sql = String.format(CANCEL_ALL_OTHER_CONNECTIONS_SQL, databaseName);
        executeSqlUpdate(sql, connection);
    }

    private void terminateOtherConnectionsToDatabase(String databaseName, Connection connection) throws SQLException {
        String sql = String.format(TERMINATE_ALL_OTHER_CONNECTIONS_SQL, databaseName);
        executeSqlUpdate(sql, connection);
    }

    private boolean doesDatabaseExist(String databaseName, Connection connection) throws SQLException {
        String sql = String.format(
            DOES_DATABASE_EXIST_SQL,
            databaseName
        );
        List<Map<String, Object>> result = executeSqlQuery(sql, connection);
        return result.size() != 0;
    }

    private ConnectionInfo connectionInfoForSnapshot(Snapshot snapshot) {
        return ConnectionInfo.Builder.
            from(originalConnectionInfo).
            setDatabase(snapshot.getName()).
            build();
    }

    private void checkSuperuser(String username, Connection connection) throws SQLException {
        String sql = "SELECT usesuper FROM pg_catalog.pg_user WHERE usename = current_user";
        List<Map<String, Object>> result = executeSqlQuery(sql, connection);
        if (!((boolean) result.get(0).get("usesuper"))) {
            throw new SQLException(
                String.
                    format(
                        "User '%s' is not a superuser. " +
                            "Using of the operations provided by this class requires superuser privileges.",
                        username));
        }
    }

    // TODO Use or delete
    private boolean doesDatabaseAllowNewConnections(String databaseName, Connection connection) throws SQLException {
        String sql = String.format(DOES_DATABASE_ALLOW_NEW_CONNECTIONS_SQL, databaseName);
        List<Map<String, Object>> result = executeSqlQuery(sql, connection);
        String allow = result.get(0).get("allow").toString();
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
}
