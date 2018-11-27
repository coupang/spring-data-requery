/*
 * Copyright 2018 Coupang Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.requery.containers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import javax.sql.DataSource;

/**
 * PostgreSQLTestContainer
 *
 * @author debop (Sunghyouk Bae)
 * @since 18. 11. 25
 */
@Slf4j
public final class PostgreSQLTestContainer {

    private PostgreSQLTestContainer() {}

    public static final String POSTGRES_VERSION = "10.6";


    public static final PostgreSQLContainer INSTANCE;
    public static final String HOST;
    public static final int PORT;
    public static final String JDBC_URL;


    static {
        INSTANCE = createPostgreSQLContainer();

        HOST = INSTANCE.getContainerIpAddress();
        PORT = INSTANCE.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
        JDBC_URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/test";
    }

    public static PostgreSQLContainer createPostgreSQLContainer() {
        PostgreSQLContainer container = new PostgreSQLContainer(PostgreSQLContainer.IMAGE + ":" + POSTGRES_VERSION);
        container.setWaitStrategy(new HostPortWaitStrategy());
        container.withLogConsumer(new Slf4jLogConsumer(log));
        container.withDatabaseName("test");
        container.start();

        return container;
    }

    @NotNull
    public static DataSource getDataSource() {
        log.debug("jdbc url={}", JDBC_URL);

        HikariConfig config = new HikariConfig();
//        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.setDriverClassName(INSTANCE.getDriverClassName());
        config.setJdbcUrl(INSTANCE.getJdbcUrl());
        config.setUsername(INSTANCE.getUsername());
        config.setPassword(INSTANCE.getPassword());

        return new HikariDataSource(config);
    }
}
