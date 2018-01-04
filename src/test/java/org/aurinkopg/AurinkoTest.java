package org.aurinkopg;

import org.junit.Test;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.HostSpec;

import java.sql.SQLException;
import java.util.Properties;

import static org.aurinkopg.TestData.POSTGRES_DATABASE;
import static org.aurinkopg.TestData.POSTGRES_USERNAME;
import static org.junit.Assert.assertEquals;

public class AurinkoTest {
    @Test
    public void smokeTest() {
        assertEquals("foo", "foo");
    }

    @Test
    public void loginTest() throws Exception {
        connect();
    }

    public PgConnection connect() throws SQLException {
        HostSpec hostSpec = new HostSpec("localhost", 5432);
        HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
        return new PgConnection(hostSpecs, POSTGRES_USERNAME, POSTGRES_DATABASE, new Properties(), "");
    }
}
