package org.aurinkopg;

import org.aurinkopg.postgresql.ConnectionInfo;
import org.aurinkopg.postgresql.Database;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;
import java.util.Map;

import static org.aurinkopg.TestData.*;

public class IntegrationTest {
    @Test
    public void integrationTest() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionInfo.Builder()
            .setHost("0.0.0.0")
            .setPort(5432)
            .setPgUsername(POSTGRES_USERNAME)
            .setPgPassword(POSTGRES_PASSWORD)
            .setDatabase(POSTGRES_DATABASE)
            .build();
        Database db = Database.connect(connectionInfo);
        Snapshot snapshot1 = null;
        try {
            JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(db.getConnection(), true));
            doSelect(jdbc);
            snapshot1 = db.takeSnapshot("snapshot1");
            doSelect(jdbc);
        } finally {
            if (snapshot1 != null) {
                db.deleteSnapshot(snapshot1);
            }
        }
    }

    private void doSelect(JdbcTemplate jdbc) {
        List<Map<String, Object>> queryResult = jdbc.queryForList("SELECT * FROM laiva JOIN valtio ON laiva.omistaja = valtio.id");
        System.out.println(queryResult);
    }
}
