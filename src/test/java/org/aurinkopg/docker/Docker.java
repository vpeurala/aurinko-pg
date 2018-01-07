package org.aurinkopg.docker;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Docker {
    private final ProcessBuilder processBuilder;

    public Docker() {
        this(Collections.emptyMap());
    }

    public Docker(Map<String, String> changedEnvironmentVariables) {
        this.processBuilder = new ProcessBuilder();
        this.processBuilder.environment().putAll(changedEnvironmentVariables);
        this.processBuilder.
            redirectInput(INHERIT).
            redirectError(INHERIT).
            redirectOutput(PIPE);
    }

    public boolean isDockerInstalledAndFoundOnPath() throws IOException, InterruptedException {
        processBuilder.
            command("which", "docker");
        Result result = runWithShortTimeout();
        return (result.exitStatus == 0);
    }

    public boolean doesDockerImageExist(String imageName) throws IOException, InterruptedException {
        processBuilder.command(
            "docker",
            "images",
            "--filter",
            "reference=" + imageName,
            "--format", "{{.Repository}}:{{.Tag}}",
            "--no-trunc");
        Result result = runWithShortTimeout();
        Optional<String> output = result.output.stream().reduce((s1, s2) -> s1 + s2);
        return (output.isPresent() && output.get().startsWith(imageName));
    }

    public Result buildImage(String imageName) throws IOException, InterruptedException {
        processBuilder.command(
            "./docker/build.sh"
        );
        return runWithLongTimeout();
    }

    public boolean isDockerContainerRunning(String containerName) throws IOException, InterruptedException {
        processBuilder.command(
            "docker",
            "ps",
            "--all",
            "--filter",
            "name=" + containerName + "",
            "--format", "{{.Names}}: {{.Status}}",
            "--no-trunc");
        Result result = runWithShortTimeout();
        Optional<String> output = result.output.stream().reduce((s1, s2) -> s1 + s2);
        return (output.isPresent() && output.get().startsWith(containerName + ": Up"));
    }

    public void buildImageAndStartContainer() throws IOException, InterruptedException {
        processBuilder.command("./docker/build.sh");
        runWithTimeout(10, MINUTES);
    }

    private Result runWithShortTimeout() throws IOException, InterruptedException {
        return runWithTimeout(2, SECONDS);
    }

    private Result runWithLongTimeout() throws IOException, InterruptedException {
        return runWithTimeout(10, MINUTES);
    }

    private Result runWithTimeout(long timeout, TimeUnit unit) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        boolean hasExited = process.waitFor(timeout, unit);
        if (!hasExited) {
            process.destroyForcibly();
        }
        int exitStatus = process.exitValue();
        InputStream inputStream = process.getInputStream();
        List<String> lines = IOUtils.readLines(inputStream, Charset.forName("UTF-8"));
        return new Result(lines, exitStatus);
    }

    public static class Result {
        private final List<String> output;
        private final int exitStatus;

        public Result(List<String> output, int exitStatus) {
            this.output = output;
            this.exitStatus = exitStatus;
        }

        public List<String> getOutput() {
            return output;
        }

        public int getExitStatus() {
            return exitStatus;
        }

        @Override
        public String toString() {
            return "Result{" +
                "output='" + output + '\'' +
                ", exitStatus=" + exitStatus +
                '}';
        }
    }
}
