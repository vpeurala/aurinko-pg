package org.aurinkopg;

import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.PGProperty;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.HostSpec;

import java.sql.SQLException;
import java.util.Properties;

import static org.aurinkopg.TestData.*;
import static org.junit.Assert.assertEquals;

public class AurinkoTest {
    @Test
    public void smokeTest() {
        assertEquals("foo", "foo");
    }

    @Test
    @Ignore // TODO
    public void loginTest() throws Exception {
        PgConnection connection = connect();
        connection.execSQLQuery("CREATE DATABASE copy_of_jaanmurtaja WITH TEMPLATE jaanmurtaja OWNER jaanmurtaja");
    }

    public PgConnection connect() throws SQLException {
        HostSpec hostSpec = new HostSpec("localhost", 5432);
        HostSpec[] hostSpecs = new HostSpec[]{hostSpec};
        Properties info = new Properties();
        PGProperty.PASSWORD.set(info, POSTGRES_PASSWORD);
        return new PgConnection(hostSpecs, POSTGRES_USERNAME, POSTGRES_DATABASE, info, "");
    }
}
