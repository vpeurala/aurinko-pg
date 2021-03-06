package org.aurinkopg.integrationtests;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.function.UnaryOperator;

public abstract class DockerUsingIntegrationTest {
    private static DockerClient dockerClient;
    private static DockerOperations dockerOperations;

    @BeforeClass
    public static void dockerSetUp() throws Exception {
        dockerOperations = new DockerOperations();
        DockerClientConfig dockerConfig = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .build();
        DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
            .withReadTimeout(10000)
            .withConnectTimeout(10000)
            .withMaxTotalConnections(10)
            .withMaxPerRouteConnections(5);
        dockerClient = DockerClientBuilder.getInstance(dockerConfig)
            .withDockerCmdExecFactory(dockerCmdExecFactory)
            .build();
        runWithRetry(dockerOperations::buildImage);
        try {
            runWithRetry(dockerOperations::createContainer);
        } catch (Throwable t) {
            System.out.printf("Caught throwable in createContainer: %s\n", t);
            t.printStackTrace();
            try {
                runWithRetry(dockerOperations::stopContainer);
                runWithRetry(dockerOperations::removeContainer);
                runWithRetry(dockerOperations::createContainer);
            } catch (Throwable t2) {
                System.out.printf("Caught throwable in createContainer retry: %s\n", t2);
                t2.printStackTrace();
                throw t2;
            }
        }
        runWithRetry(dockerOperations::startContainer);
        System.out.printf("Docker container started with id %s\n", dockerOperations.getContainerId());
        // Wait for the database to wake up...
        Thread.sleep(1000);
    }

    @AfterClass
    public static void dockerTearDown() throws Exception {
        try {
            System.out.printf("Going to stop Docker container with id %s\n", dockerOperations.getContainerId());
            runWithRetry(dockerOperations::stopContainer);
            runWithRetry(dockerOperations::removeContainer);
        } catch (Throwable t) {
            System.out.printf("Caught throwable in dockerTearDown: %s, message: %s\n", t.getClass(), t.getMessage());
            t.printStackTrace();
            throw t;
        } finally {
            runWithRetry(dockerOperations::removeContainer);
        }
    }


    private static void runWithRetry(UnaryOperator<DockerClient> dockerClientUnaryOperator) {
        int retries = 0;
        while (retries < 100) {
            try {
                dockerClientUnaryOperator.apply(dockerClient);
                break;
            } catch (NotFoundException e) {
                System.out.printf("Caught NotFoundException: %s\n", e);
                break;
            } catch (Throwable t) {
                System.out.printf("Caught exception %s, message: %s\n", t.getClass(), t.getMessage());
                if (retries < 100) {
                    retries++;
                    System.out.printf("Retrying, retry number %d\n", retries);
                } else {
                    System.out.printf("Retried 100 times, giving up.\n");
                    break;
                }
            }
        }
    }
}
