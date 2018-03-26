package org.aurinkopg.coverage;

import org.aurinkopg.postgresql.ConnectionFactory;
import org.aurinkopg.postgresql.ConnectionInfo;
import org.junit.Test;
import org.postgresql.PGProperty;

import java.util.HashMap;

public class BoilerplateTest {
    @Test
    public void invokeToString() throws Exception {
        new ConnectionInfo("", 0, "", "", "", new HashMap<>())
            .toString();
    }

    @Test
    public void invokeAddConnectionProperty() throws Exception {
        new ConnectionInfo.Builder().addConnectionProperty(PGProperty.APPLICATION_NAME, "Aurinko");
    }

    @Test
    public void invokeConstructorOfConnectionFactory() throws Exception {
        new ConnectionFactory();
    }
}
