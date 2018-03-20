package org.aurinkopg.coverage;

import org.aurinkopg.postgresql.ConnectionInfo;
import org.junit.Test;

import java.util.HashMap;

public class BoilerplateTest {
    @Test
    public void invokeToString() throws Exception {
        new ConnectionInfo("", 0, "", "", "", new HashMap<>())
            .toString();
    }
}
