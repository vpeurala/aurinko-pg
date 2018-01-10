package org.aurinkopg.postgresql;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.aurinkopg.fixtures.TestFixtures.CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER;
import static org.aurinkopg.postgresql.SqlExecutor.executeSqlQuery;
import static org.junit.Assert.assertEquals;

// TODO: These tests depend on the database being in the default initial state (like it is after running ./docker/init_db.sh).
// These tests should put the database in the required state themselves.
public class SqlExecutorTest {
    private ConnectionInfo connectionInfo;
    private ConnectionFactory connectionFactory;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connectionInfo = CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER.build();
        connection = ConnectionFactory.openConnection(connectionInfo);
    }

    @Test
    public void simpleQueryMapping() throws Exception {
        List<Map<String, Object>> result = executeSqlQuery("SELECT valtio.id, valtio.nimi FROM valtio ORDER BY id", connection);
        assertEquals(2, result.size());
        Map<String, Object> finland = result.get(0);
        assertEquals(1L, finland.get("id"));
        assertEquals("Suomi", finland.get("nimi"));
        Map<String, Object> russia = result.get(1);
        assertEquals(2L, russia.get("id"));
        assertEquals("Venäjä", russia.get("nimi"));
    }
}
