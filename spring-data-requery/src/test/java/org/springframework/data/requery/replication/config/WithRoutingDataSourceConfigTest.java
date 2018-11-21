package org.springframework.data.requery.replication.config;

import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.requery.replication.domain.User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WithRoutingDataSourceConfigTest
 *
 * @author debop
 * @since 18. 11. 21
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ReplicationDataSourceConfig.class, WithRoutingDataSourceConfig.class })
public class WithRoutingDataSourceConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityDataStore<Object> entityDataStore;

    @Test
    public void contextLoading() {
        assertThat(dataSource).isNotNull();
        assertThat(entityDataStore).isNotNull();
    }

    @Test
    @Transactional(readOnly = true)
    public void load_user_by_id_from_read_db() {
        User user = entityDataStore.findByKey(User.class, 1);
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("read_1");
    }

    @Test
    @Transactional
    public void load_user_by_id_from_write_db() {
        User user = entityDataStore.findByKey(User.class, 3);
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("write_3");
    }
}
