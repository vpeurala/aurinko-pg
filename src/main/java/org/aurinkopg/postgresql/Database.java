package org.aurinkopg.postgresql;

import org.aurinkopg.Snapshot;
import org.postgresql.jdbc.PgConnection;

import java.sql.SQLException;
import java.util.Objects;

public interface Database {
    /**
     * Create a database connection, returning a Database object
     * on which you can execute operations.
     *
     * @param connectionInfo an object which holds all the data needed to establish the connection.
     * @return a Database object.
     * @throws SQLException if obtaining a connection fails for any reason.
     */
    static Database connect(ConnectionInfo connectionInfo) throws SQLException {
        Objects.requireNonNull(connectionInfo, "Parameter connectionInfo in Database.connect(connectionInfo) cannot be null!");
        return new PostgreSQLDatabase(connectionInfo);
    }

    /**
     * TODO: Write javadoc
     *
     * @param snapshotName
     * @return
     * @throws SQLException
     */
    Snapshot takeSnapshot(String snapshotName) throws SQLException;

    /**
     * TODO: Write javadoc
     *
     * @param snapshot
     * @throws Exception
     */
    void restoreSnapshot(Snapshot snapshot) throws Exception;

    /**
     * TODO: Write javadoc
     *
     * @param snapshot
     * @throws SQLException
     */
    void deleteSnapshot(Snapshot snapshot) throws SQLException;

    PgConnection getConnection() throws SQLException;
}
