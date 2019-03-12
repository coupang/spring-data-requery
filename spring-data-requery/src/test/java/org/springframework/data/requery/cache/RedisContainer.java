package org.springframework.data.requery.cache;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * RedisContainer
 *
 * @author debop
 * @since 19. 3. 11
 */
@Slf4j
public class RedisContainer extends GenericContainer<RedisContainer> {

    public static final String IMAGE_NAME = "redis";
    public static final String DEFAULT_TAG = "4.0.11";
    public static final int EXPOSED_PORT = 6379;

    public RedisContainer() {
        this(IMAGE_NAME + ":" + DEFAULT_TAG);
    }

    public RedisContainer(String dockerImageName) {
        super(dockerImageName);

        withExposedPorts(EXPOSED_PORT);
        withLogConsumer(new Slf4jLogConsumer(log));
        setWaitStrategy(Wait.forListeningPort());

        start();
    }

    public String getHost() {
        return getContainerIpAddress();
    }

    public Integer getPort() {
        return getMappedPort(EXPOSED_PORT);
    }

    public String getUrl() {
        return "redis://" + getHost() + ":" + getPort();
    }
}
