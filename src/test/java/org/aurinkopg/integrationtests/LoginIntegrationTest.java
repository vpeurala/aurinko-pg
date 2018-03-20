package org.aurinkopg.integrationtests;

import org.aurinkopg.postgresql.ConnectionInfo;
import org.aurinkopg.postgresql.DatabaseSnapshotOperator;
import org.junit.Test;

import java.sql.SQLException;

import static org.aurinkopg.fixtures.TestFixtures.connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LoginIntegrationTest extends DockerUsingIntegrationTest {
    @Test
    public void userWithSuperuserPrivilegesCanUseDatabaseSnapshotOperator() throws Exception {
        ConnectionInfo connectionInfoForJaanmurtaja =
            connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser().build();
        DatabaseSnapshotOperator.create(connectionInfoForJaanmurtaja);
    }

    @Test
    public void userWithoutSuperuserPrivilegesCannotUseDatabaseSnapshotOperator() throws Exception {
        ConnectionInfo connectionInfoForTavis =
            connectionInfoBuilderWhichCanConnectToTestDockerContainerAsSuperuser().
                setPgUsername("tavis").
                setPgPassword("Mansikka2").
                build();
        try {
            DatabaseSnapshotOperator.create(connectionInfoForTavis);
            fail("An exception should have been thrown on the previous line.");
        } catch (SQLException e) {
            assertEquals(
                "User 'tavis' is not a superuser. " +
                    "Using of the operations provided by this class requires superuser privileges.",
                e.getMessage());
        }
    }
}
