package org.aurinkopg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.aurinkopg.postgresql.Database;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.aurinkopg.TestFixtures.CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER;
import static org.junit.Assert.assertEquals;

public class IntegrationTest {
    private static final String TEST_SELECT =
        "SELECT laiva.id AS laiva_id, " +
            "laiva.nimi AS laiva_nimi, " +
            "CAST(EXTRACT(YEAR FROM laiva.valmistumisvuosi) AS INT) AS laiva_valmistumisvuosi, " +
            "laiva.akseliteho AS laiva_akseliteho, " +
            "laiva.vetoisuus AS laiva_vetoisuus, " +
            "laiva.pituus AS laiva_pituus, " +
            "laiva.leveys AS laiva_leveys, " +
            "valtio.id AS valtio_id, " +
            "valtio.nimi AS valtio_nimi " +
            "FROM laiva " +
            "INNER JOIN valtio " +
            "ON laiva.omistaja = valtio.id";

    private Database database;
    private JdbcTemplate jdbc;
    private ObjectMapper objectMapper;
    private Snapshot snapshot;

    @Before
    public void setUp() throws Exception {
        database = Database.connect(CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER.build());
        jdbc = new JdbcTemplate(new SingleConnectionDataSource(database.getConnection(), false));
        objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void takingSnapshotDoesNotChangeData() throws Exception {
        String beforeSnapshot = selectDatabaseState();
        snapshot = database.takeSnapshot("snapshot1");
        String afterSnapshot = selectDatabaseState();
        assertEquals(beforeSnapshot, afterSnapshot);
        database.deleteSnapshot(snapshot);
    }

    @Test
    public void restoringSnapshotRestoresAllDataBackToOriginal() throws Exception {
        String beforeSnapshot = selectDatabaseState();
        assertEquals(initialStateJson(), beforeSnapshot);
        snapshot = database.takeSnapshot("snapshot1");
        enlargeUrhoAndSellItToRussia();
        assertEquals(jsonResource("/state_after_enlarging_urho_and_selling_it_to_russia.json"), selectDatabaseState());
        database.restoreSnapshot(snapshot);
        assertEquals(initialStateJson(), selectDatabaseState());
        database.deleteSnapshot(snapshot);
    }

    private String initialStateJson() throws IOException {
        return jsonResource("/initial_state.json");
    }

    private String jsonResource(String resourceName) throws IOException {
        String resource = IOUtils.resourceToString(resourceName, Charset.forName("UTF-8"));
        JsonNode jsonNode = objectMapper.readTree(resource);
        return objectMapper.writeValueAsString(jsonNode);
    }

    private String selectDatabaseState() throws JsonProcessingException {
        List<Map<String, Object>> queryResult = jdbc.queryForList(TEST_SELECT);
        return objectMapper.writeValueAsString(queryResult);
    }

    private void enlargeUrhoAndSellItToRussia() {
        jdbc.update("UPDATE laiva SET pituus = 220, leveys = 42, omistaja = (SELECT id FROM valtio WHERE nimi = 'Venäjä') WHERE nimi = 'Urho'");
    }
}
