package org.aurinkopg.docker;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.aurinkopg.fixtures.TestFixtures.TEST_DOCKER_CONTAINER_NAME;
import static org.aurinkopg.fixtures.TestFixtures.TEST_DOCKER_IMAGE_NAME;
import static org.junit.Assert.assertEquals;

public class DockerTest {
    private Docker docker;

    @Before
    public void setUp() throws Exception {
        docker = new Docker(TEST_DOCKER_IMAGE_NAME, TEST_DOCKER_CONTAINER_NAME);
    }

    @Test
    public void dockerIsFoundAsItShouldBe() throws Exception {
        assertEquals(
            "Docker not found on path in a test where it should be found.\n" +
                "It seems like you actually don't have Docker installed, or it is not on your PATH.\n" +
                "Install it now from 'https://store.docker.com/search?type=edition&offering=community' before proceeding.\n",
            true,
            docker.isDockerInstalledAndFoundOnPath());
    }

    @Test
    public void dockerIsNotFound() throws Exception {
        Map<String, String> environment = new HashMap<>();
        environment.put("PATH", "");
        assertEquals(false, new Docker(
            TEST_DOCKER_IMAGE_NAME,
            TEST_DOCKER_CONTAINER_NAME,
            environment).
            isDockerInstalledAndFoundOnPath());
    }

    @Test
    public void dockerContainerJaanmurtajaDbIsRunning() throws Exception {
        assertEquals(true, docker.isDockerContainerRunning());
    }

    @Test
    public void dockerImageAurinkoPostgreSQLIsFound() throws Exception {
        assertEquals(true, docker.doesDockerImageExist());
    }

    @Test
    public void testBuildDockerContainer() throws Exception {
        Docker.Result result = docker.buildImage();
        assertEquals(result.toString(), 0, result.getExitStatus());
    }
}
