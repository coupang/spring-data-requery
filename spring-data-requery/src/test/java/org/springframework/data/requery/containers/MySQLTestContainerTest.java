package org.springframework.data.requery.containers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MySQLTestContainerTest
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
public class MySQLTestContainerTest {

    @Test
    public void create_mysql_test_container() throws Exception {

        MySQLContainer container = MySQLTestContainer.INSTANCE;
        assertThat(container).isNotNull();

        DataSource dataSource = MySQLTestContainer.getDataSource();
        Connection connection = dataSource.getConnection();

        assertThat(connection).isNotNull();

        connection.close();
    }
}
