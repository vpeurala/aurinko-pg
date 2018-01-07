package org.aurinkopg.docker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DockerTest {
    @Test
    public void smoke() throws Exception {
        assertEquals(true, new Docker().isDockerInstalledAndFoundOnPath());
    }
}
