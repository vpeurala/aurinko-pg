package org.aurinkopg;

import org.aurinkopg.postgresql.ConnectionInfo;

public class TestFixtures {
    public static final String POSTGRES_USERNAME = "jaanmurtaja";
    public static final String POSTGRES_PASSWORD = "argxBX4DxWJKC7st";
    public static final String POSTGRES_DATABASE = "jaanmurtaja";
    public static final String TEST_DOCKER_IMAGE_NAME = "aurinko/postgresql-9.5.5:latest";
    public static final String TEST_DOCKER_CONTAINER_NAME = "jaanmurtaja-db";

    public static final ConnectionInfo.Builder CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER =
        new ConnectionInfo.Builder()
            .setHost("0.0.0.0")
            .setPort(5432)
            .setPgUsername(POSTGRES_USERNAME)
            .setPgPassword(POSTGRES_PASSWORD)
            .setDatabase(POSTGRES_DATABASE);
}
