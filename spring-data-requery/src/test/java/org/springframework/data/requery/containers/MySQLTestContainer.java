package org.springframework.data.requery.containers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import javax.sql.DataSource;

/**
 * MySQLTestContainer
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
public final class MySQLTestContainer {

    private MySQLTestContainer() {}

    public static final String MYSQL_VERION = "5.7";

    public static final MySQLContainer INSTANCE;
    public static final String HOST;
    public static final Integer PORT;

    static {
        INSTANCE = createMySQLContainer();

        HOST = INSTANCE.getContainerIpAddress();
        PORT = INSTANCE.getMappedPort(MySQLContainer.MYSQL_PORT);
    }

    public static MySQLContainer createMySQLContainer() {
        MySQLContainer container = new MySQLContainer();

        container.setWaitStrategy(new HostPortWaitStrategy());
        container.withLogConsumer(new Slf4jLogConsumer(log));
        container.withDatabaseName("test");

        container.start();
        return container;
    }

    @NotNull
    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(INSTANCE.getDriverClassName());
        config.setJdbcUrl(INSTANCE.getJdbcUrl());
        config.setUsername(INSTANCE.getUsername());
        config.setPassword(INSTANCE.getPassword());

        return new HikariDataSource(config);
    }
}
