package org.aurinkopg;

import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.HostSpec;

import java.sql.SQLException;
import java.util.Properties;

public class Snapshot {
    public PgConnection connect(ConnectionInfo connectionInfo) throws SQLException {
        HostSpec hostSpec = new HostSpec(connectionInfo.getHost(), connectionInfo.getPort());
        HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
        Properties info = new Properties();
        PGProperty.PASSWORD.set(info, connectionInfo.getPgPassword());
        return new PgConnection(hostSpecs, connectionInfo.getPgUsername(), connectionInfo.getDatabase(), info, connectionInfo.getJdbcUrl());
    }
}
