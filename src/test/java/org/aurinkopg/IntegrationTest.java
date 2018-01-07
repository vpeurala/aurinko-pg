package org.aurinkopg;

import org.aurinkopg.postgresql.Database;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;
import java.util.Map;

import static org.aurinkopg.TestFixtures.CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER;

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
    public void integrationTest() throws Exception {
        try {
            doSelect(jdbc);
            snapshot = database.takeSnapshot("snapshot1");
            doSelect(jdbc);
        } finally {
            if (snapshot != null) {
                database.deleteSnapshot(snapshot);
            }
        }
    }

    private void doSelect(JdbcTemplate jdbc) {
        List<Map<String, Object>> queryResult = jdbc.queryForList("SELECT * FROM laiva JOIN valtio ON laiva.omistaja = valtio.id");
        System.out.println(queryResult);
    }
}
