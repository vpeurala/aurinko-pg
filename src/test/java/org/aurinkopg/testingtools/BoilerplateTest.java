package org.aurinkopg.testingtools;

import org.aurinkopg.postgresql.ConnectionInfo;
import org.aurinkopg.postgresql.SqlExecutor;
import org.junit.Test;

import java.util.HashMap;

public class BoilerplateTest {
    @Test
    public void invokeConstructors() throws Exception {
        new SqlExecutor();
    }

    @Test
    public void invokeToString() throws Exception {
        new ConnectionInfo("", 0, "", "", "", new HashMap<>());
    }
}
