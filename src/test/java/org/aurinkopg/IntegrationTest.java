package org.aurinkopg;

import org.aurinkopg.postgresql.ConnectionInfo;
import org.aurinkopg.postgresql.Database;
import org.junit.Test;

import java.sql.Connection;

import static org.aurinkopg.TestData.*;

public class IntegrationTest {
    @Test
    public void integrationTest() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo.Builder()
            .setHost("0.0.0.0")
            .setPort(5432)
            .setPgUsername(POSTGRES_USERNAME)
            .setPgPassword(POSTGRES_PASSWORD)
            .setDatabase(POSTGRES_DATABASE)
            .build();
        Database db = Database.connect(connectionInfo);
        Snapshot snapshot1 = db.takeSnapshot();
        Connection connection = db.getConnection();
        connection.
    }
}
