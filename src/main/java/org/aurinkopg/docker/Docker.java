package org.aurinkopg.docker;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Docker {
    public boolean isDockerInstalledAndFoundOnPath() throws IOException, InterruptedException {
        Process whichDocker = new ProcessBuilder().
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
