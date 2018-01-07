package org.aurinkopg.docker;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Docker {
    private final Map<String, String> changedEnvironmentVariables;

    public Docker() {
        this.changedEnvironmentVariables = Collections.emptyMap();
    }

    public Docker(Map<String, String> changedEnvironmentVariables) {
        this.changedEnvironmentVariables = changedEnvironmentVariables;
    }

    public boolean isDockerInstalledAndFoundOnPath() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.environment().putAll(changedEnvironmentVariables);
        Process whichDocker = processBuilder.
            inheritIO().
            command("which", "docker").
            start();
        boolean hasExited = whichDocker.waitFor(3, TimeUnit.SECONDS);
        if (!hasExited) {
            whichDocker.destroyForcibly();
        }
        int exitStatus = whichDocker.exitValue();
        return (exitStatus == 0);
    }
}
