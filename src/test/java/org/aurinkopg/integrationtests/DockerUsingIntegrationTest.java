package org.aurinkopg.integrationtests;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class DockerUsingIntegrationTest {
    public static final String IMAGE_NAME = "vpeurala/aurinko-pg-9.5.5:latest";
    public static final String CONTAINER_NAME = "jaanmurtaja-db";
    public static final String POSTGRES_HOST = "jaanmurtaja-db";
    public static final ExposedPort POSTGRES_IMAGE_PORT = ExposedPort.tcp(5432);
    public static final int POSTGRES_CONTAINER_PORT = 6543;
    protected static DockerClient dockerClient;
    protected static String containerId;

    @BeforeClass
    public static void dockerSetUp() throws Exception {
        DockerClientConfig dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
            .withReadTimeout(1000)
            .withConnectTimeout(1000)
            .withMaxTotalConnections(1)
            .withMaxPerRouteConnections(1);
        dockerClient = DockerClientBuilder.getInstance(dockerConfig)
            .withDockerCmdExecFactory(dockerCmdExecFactory)
            .build();
        buildImage();
        try {
            createContainer();
        } catch (Throwable t) {
            System.out.printf("Caught throwable in createContainer: %s\n", t);
            t.printStackTrace();
            try {
                stopContainer();
                removeContainer();
                createContainer();
            } catch (Throwable t2) {
                System.out.printf("Caught throwable in createContainer retry: %s\n", t2);
                t2.printStackTrace();
                throw t2;
            }
        }
        dockerClient.startContainerCmd(containerId).exec();
        System.out.printf("Docker container started with id %s\n", containerId);
    }

    @AfterClass
    public static void dockerTearDown() throws Exception {
        try {
            System.out.printf("Going to stop Docker container with id %s\n", containerId);
            stopContainer();
            removeContainer();
        } catch (Throwable t) {
            System.out.printf("Caught throwable in dockerTearDown: %s\n", t);
            t.printStackTrace();
            throw t;
        } finally {
            removeContainer();
        }
    }

    /**
     * Equivalent shell command:
     * docker build --file Dockerfile --tag vpeurala/aurinko-pg-9.5.5:latest .;
     */
    private static void buildImage() {
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
    }

    /**
     * Equivalent shell command:
     * docker run --detach --hostname jaanmurtaja-db --name jaanmurtaja-db --publish 6543:5432 --user jaanmurtaja vpeurala/aurinko-pg-9.5.5:latest;
     */
    private static void createContainer() {
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
                        new Binding("localhost", String.valueOf(POSTGRES_CONTAINER_PORT)), POSTGRES_IMAGE_PORT))
                .exec();
        } catch (Throwable t) {
            System.out.printf("Caught throwable while creating container: %s\n", t);
            t.printStackTrace();
            throw t;
        }
        containerId = createContainerResponse.getId();
    }

    private static void removeContainer() {
        List<Container> containers = dockerClient.listContainersCmd().withLabelFilter(CONTAINER_NAME).exec();
        containers.stream().filter(c -> c.getImageId().equals(IMAGE_NAME)).forEach(c -> {
            dockerClient.removeContainerCmd(c.getId()).withForce(true).exec();
        });
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
    }

    private static void stopContainer() {
        dockerClient.stopContainerCmd(containerId).exec();
    }
}
