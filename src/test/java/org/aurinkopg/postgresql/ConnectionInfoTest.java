package org.aurinkopg.postgresql;

import org.junit.Test;

import static org.aurinkopg.fixtures.TestFixtures.*;
import static org.junit.Assert.assertEquals;

public class ConnectionInfoTest {
    private ConnectionInfo connectionInfo;

    @Test
    public void canGenerateCorrectJdbcUrl() {
        connectionInfo = new ConnectionInfo.Builder().
            setHost(POSTGRES_HOST).
            setPort(POSTGRES_PORT).
            setPgUsername(POSTGRES_USERNAME).
            setPgPassword(POSTGRES_PASSWORD).
            setDatabase(POSTGRES_DATABASE).
            build();
        assertEquals("jdbc:postgresql://0.0.0.0:5432/jaanmurtaja", connectionInfo.getJdbcUrl());
    }
}
