package org.aurinkopg.integrationtests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.aurinkopg.Snapshot;
import org.aurinkopg.datasourceadapter.DataSourceAdapter;
import org.aurinkopg.locale.FinnishLocaleUtil;
import org.aurinkopg.postgresql.ConnectionInfo;
import org.aurinkopg.postgresql.DatabaseSnapshotOperator;
import org.aurinkopg.testingtools.JsonResourceUser;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import static org.aurinkopg.fixtures.TestFixtures.SELECT_WHOLE_DATASET_SQL;
import static org.aurinkopg.fixtures.TestFixtures.connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SnapshotIntegrationTest extends DockerUsingIntegrationTest implements JsonResourceUser {
    private ConnectionInfo connectionInfo;
    private DatabaseSnapshotOperator database;
    private DataSource dataSource;
    private JdbcTemplate jdbc;
    private DataSourceTransactionManager transactionManager;

    @Before
    public void setUp() throws Exception {
        connectionInfo = connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser().build();
        database = DatabaseSnapshotOperator.create(connectionInfo);
        dataSource = new DataSourceAdapter(database);
        transactionManager = new DataSourceTransactionManager(dataSource);
        jdbc = new JdbcTemplate(this.dataSource);
    }

    @Test
    public void takingSnapshotDoesNotChangeData() throws Exception {
        String beforeSnapshot = selectDatabaseState();
        database.takeSnapshot("snapshot1");
        String afterSnapshot = selectDatabaseState();
        assertEquals(beforeSnapshot, afterSnapshot);
    }

    @Test
    public void takingSnapshotTerminatesOtherConnectionsToTheDatabase() throws Exception {
        // Create 10 connections; these will get killed when the snapshot is taken.
        for (int i = 0; i < 10; i++) {
            database.openConnection();
        }
        database.takeSnapshot("snapshot_which_kills_other_connections");
        // Ensure that we can still open a new connection after the snapshot is taken.
        database.openConnection();
    }

    @Test
    public void restoringSnapshotRestoresAllDataBackToOriginal() throws Exception {
        assertEquals(initialStateJson(), selectDatabaseState());
        Snapshot snapshot1 = database.takeSnapshot("snapshot1");
        enlargeUrhoAndSellItToRussia();
        assertEquals(jsonResource("/state_after_enlarging_urho_and_selling_it_to_russia.json"), selectDatabaseState());
        database.restoreSnapshot(snapshot1);
        assertEquals(initialStateJson(), selectDatabaseState());
    }

    @Test
    public void multipleSnapshots() throws Exception {
        assertEquals(initialStateJson(), selectDatabaseState());
        Snapshot initialStateSnapshot = database.takeSnapshot("initial_state_snapshot");
        enlargeUrhoAndSellItToRussia();
        Snapshot afterEnlargingUrhoAndSellingItToRussia = database.takeSnapshot("after_sales_snapshot");
        database.restoreSnapshot(initialStateSnapshot);
        assertEquals(initialStateJson(), selectDatabaseState());
        database.restoreSnapshot(afterEnlargingUrhoAndSellingItToRussia);
        assertEquals(stateAfterEnlargingUrhoAndSellingItToRussia(), selectDatabaseState());
        database.restoreSnapshot(initialStateSnapshot);
        assertEquals(initialStateJson(), selectDatabaseState());
    }

    @Test
    public void snapshotCanBeDeleted() throws Exception {
        Snapshot snapshot = database.takeSnapshot("snapshot_which_will_be_deleted");
        database.deleteSnapshot(snapshot);
        // Restoring the snapshot does not work anymore, since it has been deleted.
        try {
            database.restoreSnapshot(snapshot);
            fail("An exception should have been thrown.");
        } catch (PSQLException e) {
            assertEquals("FATAL: database \"snapshot_which_will_be_deleted\" does not exist", e.getMessage());
        }
    }

    private String initialStateJson() throws IOException {
        return jsonResource("/initial_state.json");
    }

    private String stateAfterEnlargingUrhoAndSellingItToRussia() throws IOException {
        return jsonResource("/state_after_enlarging_urho_and_selling_it_to_russia.json");
    }

    private String selectDatabaseState() throws JsonProcessingException, SQLException {
        List<Map<String, Object>> queryResult = jdbc.queryForList(SELECT_WHOLE_DATASET_SQL);
        return objectMapper().writeValueAsString(queryResult);
    }

    private TransactionDefinition createTransactionDefinition() {
        DateTimeFormatter formatter =
            DateTimeFormatter.
                ofLocalizedDateTime(FormatStyle.MEDIUM).
                withLocale(FinnishLocaleUtil.finnishLocale()).
                withZone(FinnishLocaleUtil.finnishTimeZoneId());
        Instant now = Instant.now();
        String formattedNow = formatter.format(now);
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setReadOnly(false);
        transactionDefinition.setName("Spring Transaction " + formattedNow);
        return transactionDefinition;
    }

    private void enlargeUrhoAndSellItToRussia() {
        TransactionDefinition tx = createTransactionDefinition();
        TransactionStatus txStatus = this.transactionManager.getTransaction(tx);
        jdbc.update("UPDATE laiva " +
            "SET pituus = 220, " +
            "leveys = 42, " +
            "vetoisuus = 20000, " +
            "omistaja = " +
            "(SELECT id FROM valtio WHERE nimi = 'Venäjä') " +
            "WHERE nimi = 'Urho'");
        this.transactionManager.commit(txStatus);
    }
}
