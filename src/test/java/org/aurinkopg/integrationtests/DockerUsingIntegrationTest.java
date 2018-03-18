package org.aurinkopg.integrationtests;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
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

public abstract class DockerUsingIntegrationTest {
    public static final String IMAGE_NAME = "vpeurala/aurinko-pg-9.5.5:latest";
    public static final String CONTAINER_NAME = "jaanmurtaja-db";
    public static final String POSTGRES_HOST = "jaanmurtaja-db";
    public static final ExposedPort POSTGRES_PORT = ExposedPort.tcp(5432);
    protected static DockerClient dockerClient;
    protected static String containerId;

    @BeforeClass
    public static void dockerSetUp() throws Exception {
        DockerClientConfig dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
            .withReadTimeout(1000)
            .withConnectTimeout(1000)
            .withMaxTotalConnections(100)
            .withMaxPerRouteConnections(10);
        dockerClient = DockerClientBuilder.getInstance(dockerConfig)
            .withDockerCmdExecFactory(dockerCmdExecFactory)
            .build();
        // Equivalent shell command:
        // docker build --file Dockerfile --tag vpeurala/aurinko-pg-9.5.5:latest .;
        dockerClient.buildImageCmd(new File("docker"))
            .withTags(new HashSet<>(Collections.singletonList(IMAGE_NAME)))
            .exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    System.out.println(item.getStream());
                    super.onNext(item);
                }
            })
            .awaitImageId();
        // Equivalent shell command:
        // docker run --detach --hostname jaanmurtaja-db --name jaanmurtaja-db --publish 6543:5432 --user jaanmurtaja vpeurala/aurinko-pg-9.5.5:latest;
        CreateContainerResponse createContainerResponse = dockerClient
            .createContainerCmd(IMAGE_NAME)
            .withAliases(POSTGRES_HOST)
            .withExposedPorts(POSTGRES_PORT)
            .withHostName(CONTAINER_NAME)
            .withName(CONTAINER_NAME)
            .withPortBindings(new PortBinding(new Ports.Binding("localhost", "6543"), POSTGRES_PORT))
            .exec();
        containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();
    }

    @AfterClass
    public static void dockerTearDown() throws Exception {
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
    }
}
