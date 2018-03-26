package org.aurinkopg.postgresql;

import org.aurinkopg.Snapshot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public interface DatabaseSnapshotOperator {
    /**
     * Create a database connection, returning a Database object
     * on which you can execute operations.
     *
     * @param connectionInfo an object which holds all the data needed to establish the connection.
     * @return a Database object.
     * @throws SQLException if obtaining a connection fails for any reason.
     */
    static DatabaseSnapshotOperator create(ConnectionInfo connectionInfo) throws SQLException {
        Objects.requireNonNull(connectionInfo, "Parameter connectionInfo in Database.connect(connectionInfo) cannot be null!");
        return new PostgreSQLDatabaseSnapshotOperator(connectionInfo);
    }

    /**
     * Take a snapshot of the current database.
     *
     * @param snapshotName the name for the snapshot.
     * @return the snapshot taken.
     * @throws SQLException if something goes wrong.
     */
    Snapshot takeSnapshot(String snapshotName) throws SQLException;

    /**
     * Restore the database state from a snapshot.
     *
     * @param snapshot the snapshot to restore.
     * @throws Exception if something goes wrong.
     */
    void restoreSnapshot(Snapshot snapshot) throws Exception;

    /**
     * Delete a snapshot.
     *
     * @param snapshot the snapshot to delete.
     * @throws SQLException if something goes wrong.
     */
    void deleteSnapshot(Snapshot snapshot) throws SQLException;

    Connection openConnection() throws SQLException;
}
