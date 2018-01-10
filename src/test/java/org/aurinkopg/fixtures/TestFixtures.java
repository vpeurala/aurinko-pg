package org.aurinkopg.fixtures;

import org.aurinkopg.postgresql.ConnectionInfo;

public class TestFixtures {
    public static final String POSTGRES_HOST = "0.0.0.0";
    public static final Integer POSTGRES_PORT = 5432;
    public static final String POSTGRES_USERNAME = "jaanmurtaja";
    public static final String POSTGRES_PASSWORD = "argxBX4DxWJKC7st";
    public static final String POSTGRES_DATABASE = "jaanmurtaja";
    public static final String TEST_DOCKER_IMAGE_NAME = "vpeurala/aurinko-pg-9.5.5:latest";
    public static final String TEST_DOCKER_CONTAINER_NAME = "jaanmurtaja-db";

    public static final String SELECT_WHOLE_DATASET_SQL =
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
            "ON laiva.omistaja = valtio.id " +
            "ORDER BY laiva_id";

    public static final ConnectionInfo.Builder CONNECTION_INFO_BUILDER_WHICH_CONNECTS_TO_TEST_DOCKER_CONTAINER =
        new ConnectionInfo.Builder()
            .setHost(POSTGRES_HOST)
            .setPort(POSTGRES_PORT)
            .setPgUsername(POSTGRES_USERNAME)
            .setPgPassword(POSTGRES_PASSWORD)
            .setDatabase(POSTGRES_DATABASE);
}
