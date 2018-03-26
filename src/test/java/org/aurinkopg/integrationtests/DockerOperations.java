package org.aurinkopg.integrationtests;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.BuildImageResultCallback;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DockerOperations {
    public static final String IMAGE_NAME = "vpeurala/aurinko-pg-9.5.5:latest";
    public static final String CONTAINER_NAME = "jaanmurtaja-db";
    public static final String POSTGRES_HOST = "jaanmurtaja-db";
    public static final ExposedPort POSTGRES_IMAGE_PORT = ExposedPort.tcp(5432);
    public static final int POSTGRES_CONTAINER_PORT = 6543;
    private String containerId;

    /**
     * Equivalent shell command:
     * docker build --file Dockerfile --tag vpeurala/aurinko-pg-9.5.5:latest .;
     */
    public DockerClient buildImage(DockerClient dockerClient) {
        String imageId;
        try {
            imageId = dockerClient.buildImageCmd(new File("docker"))
                .withTags(new HashSet<>(Collections.singletonList(IMAGE_NAME)))
                .exec(new BuildImageResultCallback() {
                    @Override
                    public void onNext(BuildResponseItem item) {
                        if (item != null && item.getStream() != null) {
                            System.out.println(
                                item
                                    .getStream()
                                    .trim()
                                    .replaceAll("[ \f\n\r\t]+", " "));
                        }
                        super.onNext(item);
                    }
                })
                .awaitImageId();
        } catch (Throwable t) {
            System.out.printf("Caught throwable while building image: %s\n", t);
            t.printStackTrace();
            throw t;
        }
        System.out.printf("Docker image built with image id %s\n", imageId);
        return dockerClient;
    }

    /**
     * Equivalent shell command:
     * docker run --detach --hostname jaanmurtaja-db --name jaanmurtaja-db --publish 6543:5432 --user jaanmurtaja vpeurala/aurinko-pg-9.5.5:latest;
     */
    public DockerClient createContainer(DockerClient dockerClient) {
        CreateContainerResponse createContainerResponse;
        try {
            createContainerResponse = dockerClient
                .createContainerCmd(IMAGE_NAME)
                .withExposedPorts(POSTGRES_IMAGE_PORT)
                .withHostName(CONTAINER_NAME)
                .withName(CONTAINER_NAME)
                .withNetworkMode("bridge")
                .withPortBindings(
                    new PortBinding(
                        new Ports.Binding("localhost", String.valueOf(POSTGRES_CONTAINER_PORT)), POSTGRES_IMAGE_PORT))
                .exec();
        } catch (Throwable t) {
            System.out.printf("Caught throwable while creating container: %s\n", t);
            t.printStackTrace();
            throw t;
        }
        setContainerId(createContainerResponse.getId());
        return dockerClient;
    }

    public DockerClient removeContainer(DockerClient dockerClient) {
        List<Container> containers = dockerClient.listContainersCmd().withLabelFilter(CONTAINER_NAME).exec();
        containers.stream().filter(c -> c.getImageId().equals(IMAGE_NAME)).forEach(c -> {
            dockerClient.removeContainerCmd(c.getId()).withForce(true).exec();
        });
        dockerClient.removeContainerCmd(getContainerId()).withForce(true).exec();
        return dockerClient;
    }

    public DockerClient stopContainer(DockerClient dockerClient) {
        dockerClient.stopContainerCmd(getContainerId()).exec();
        return dockerClient;
    }

    public DockerClient startContainer(DockerClient dockerClient) {
        dockerClient.startContainerCmd(getContainerId()).exec();
        return dockerClient;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
