package org.aurinkopg.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aurinkopg.postgresql.ConnectionFactory;
import org.aurinkopg.postgresql.ConnectionInfo;
import org.aurinkopg.testingtools.JsonResourceUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.aurinkopg.fixtures.TestFixtures.SELECT_WHOLE_DATASET_SQL;
import static org.aurinkopg.fixtures.TestFixtures.connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser;
import static org.aurinkopg.postgresql.ConnectionFactory.openConnection;
import static org.aurinkopg.postgresql.SqlExecutor.executeSqlQuery;
import static org.junit.Assert.assertEquals;

// TODO: These tests depend on the database being in the default initial state (like it is after running ./docker/init_db.sh).
// These tests should put the database in the required state themselves.
public class SqlExecutorIntegrationTest extends DockerUsingIntegrationTest implements JsonResourceUser {
    private ConnectionInfo connectionInfo;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        connectionInfo = connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser().build();
        connection = openConnection(connectionInfo);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
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

    @Test
    public void complexQueryMapping() throws Exception {
        List<Map<String, Object>> result = executeSqlQuery(SELECT_WHOLE_DATASET_SQL, connection);
        assertEquals(jsonResource("/initial_state.json"), objectMapper().writeValueAsString(result));
    }
}
