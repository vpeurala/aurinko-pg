package org.aurinkopg.docker;

import org.aurinkopg.testingtools.JavaTee;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Docker {
    private final String imageName;
    private final String containerName;
    private final ProcessBuilder processBuilder;

    public Docker(String imageName, String containerName) {
        this(
            imageName,
            containerName,
            Collections.emptyMap());
    }

    public Docker(
        String imageName,
        String containerName,
        Map<String, String> changedEnvironmentVariables) {
        this.imageName = imageName;
        this.containerName = containerName;
        this.processBuilder = new ProcessBuilder();
        this.processBuilder.environment().putAll(changedEnvironmentVariables);
        this.processBuilder.
            redirectInput(INHERIT).
            redirectError(INHERIT).
            redirectOutput(PIPE);
    }

    public boolean isDockerInstalledAndFoundOnPath() throws IOException, InterruptedException {
        processBuilder.command("which", "docker");
        Result result = runWithTimeout(3, SECONDS);
        return (result.exitStatus == 0);
    }

    public boolean doesDockerImageExist() throws IOException, InterruptedException {
        processBuilder.command(
            "docker",
            "images",
            "--filter",
            "reference=" + imageName,
            "--format", "{{.Repository}}:{{.Tag}}",
            "--no-trunc");
        Result result = runWithTimeout(3, SECONDS);
        Optional<String> output = result.output.stream().reduce((s1, s2) -> s1 + s2);
        return (output.isPresent() && output.get().startsWith(imageName));
    }

    public Result buildImage() throws IOException, InterruptedException {
        processBuilder.command("./docker/build.sh");
        return runWithTimeout(15, MINUTES);
    }

    public Result runContainer() throws IOException, InterruptedException {
        if (doesDockerImageExist()) {
            throw new IllegalStateException("Docker image '" + imageName + "' does not exist yet. " +
                "Build it first and then start the container.");
        }
        if (isDockerContainerRunning()) {
            return new Result(asList("Container is already running."), 1);
        } else {
            processBuilder.command("./docker/run.sh");
            return runWithTimeout(15, SECONDS);
        }
    }

    public Result forceRestartContainer() throws IOException, InterruptedException {
        // TODO Implement this
        return null;
    }

    public boolean isDockerContainerRunning() throws IOException, InterruptedException {
        processBuilder.command(
            "docker",
            "ps",
            "--all",
            "--filter",
            "name=" + containerName + "",
            "--format", "{{.Names}}: {{.Status}}",
            "--no-trunc");
        Result result = runWithTimeout(3, SECONDS);
        Optional<String> output = result.output.stream().reduce((s1, s2) -> s1 + s2);
        return (output.isPresent() && output.get().startsWith(containerName + ": Up"));
    }

    private Result runWithTimeout(long timeout, TimeUnit unit) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        boolean hasExited = process.waitFor(timeout, unit);
        if (!hasExited) {
            process.destroyForcibly();
        }
        int exitStatus = process.exitValue();
        InputStream inputStream = process.getInputStream();
        List<String> lines = JavaTee.readLines(inputStream);
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
                "output='" + output.stream().reduce((s1, s2) -> s1 + "\n" + s2) + '\'' +
                ", exitStatus=" + exitStatus +
                '}';
        }
    }
}
