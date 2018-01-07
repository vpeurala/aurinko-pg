package org.aurinkopg.docker;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.aurinkopg.TestFixtures.TEST_DOCKER_CONTAINER_NAME;
import static org.junit.Assert.assertEquals;

public class DockerTest {
    @Test
    public void dockerIsFoundAsItShouldBe() throws Exception {
        assertEquals(
            "Docker not found on path in a test where it should be found.\n" +
                "It seems like you actually don't have Docker installed, or it is not on your PATH.\n" +
                "Install it now from 'https://store.docker.com/search?type=edition&offering=community' before proceeding.\n",
            true,
            new Docker().isDockerInstalledAndFoundOnPath());
    }

    @Test
    public void dockerIsNotFound() throws Exception {
        Map<String, String> environment = new HashMap<>();
        environment.put("PATH", "");
        assertEquals(false, new Docker(environment).isDockerInstalledAndFoundOnPath());
    }

    @Test
    public void dockerContainerJaanmurtajaDbIsRunning() throws Exception {
        assertEquals(true, new Docker().isDockerContainerRunning(TEST_DOCKER_CONTAINER_NAME));
    }
}
