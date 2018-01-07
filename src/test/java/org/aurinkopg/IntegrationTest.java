package org.aurinkopg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.aurinkopg.postgresql.Database;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;
import java.util.Map;

import static org.aurinkopg.TestFixtures.CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER;
import static org.junit.Assert.assertEquals;

public class IntegrationTest {
    private Database database;
    private Snapshot snapshot;
    private JdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception {
        database = Database.connect(CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER.build());
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(database.getConnection(), false));
    }

    @Test
    public void takingSnapshotDoesNotChangeData() throws Exception {
        try {
            String beforeSnapshot = doSelect(jdbc);
            snapshot = database.takeSnapshot("snapshot1");
            String afterSnapshot = doSelect(jdbc);
            assertEquals(beforeSnapshot, afterSnapshot);
        } finally {
            if (snapshot != null) {
                database.deleteSnapshot(snapshot);
            }
        }
    }

    private String doSelect(JdbcTemplate jdbc) throws JsonProcessingException {
        List<Map<String, Object>> queryResult = jdbc.queryForList("SELECT * FROM laiva JOIN valtio ON laiva.omistaja = valtio.id");
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(queryResult);
    }
}
