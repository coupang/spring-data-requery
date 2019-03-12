package org.springframework.data.requery.cache.redis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.requery.TransactionIsolation;
import io.requery.meta.EntityModel;
import io.requery.query.Result;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.requery.cache.AbstractRedisCacheTest;
import org.springframework.data.requery.cache.redis.RedisEntityCacheTest.TestConfiguration;
import org.springframework.data.requery.configs.AbstractRequeryConfiguration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.Models;
import org.springframework.data.requery.domain.RandomData;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.listeners.LogbackListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RedisEntityCacheTest
 *
 * @author debop
 * @since 19. 3. 11
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { TestConfiguration.class })
@Transactional
public class RedisEntityCacheTest extends AbstractRedisCacheTest {

    @org.springframework.context.annotation.Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class TestConfiguration extends AbstractRequeryConfiguration {

        @Override
        @Bean
        public EntityModel getEntityModel() {
            return Models.DEFAULT;
        }

        @Bean
        public DataSource dataSource() {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
            config.setUsername("sa");

            DataSource dataSource = new HikariDataSource(config);
            log.trace("DataSource={}", dataSource);
            return dataSource;
        }

        @Override
        @Bean
        public io.requery.sql.Configuration requeryConfiguration(@Nonnull DataSource dataSource, @Nonnull EntityModel entityModel) {
            return new ConfigurationBuilder(dataSource, entityModel)
                // .useDefaultLogging()
                .setEntityCache(new RedisEntityCache(entityModel, getRedissonClient()))
                .setStatementCacheSize(1024)
                .setBatchUpdateSize(100)
                .addStatementListener(new LogbackListener<>())
                .setTransactionIsolation(TransactionIsolation.READ_COMMITTED)
                .build();
        }
    }


    @Autowired
    protected EntityDataStore<Object> dataStore;

    @Autowired
    protected RequeryOperations requeryOperations;

    @Before
    public void setup() {
        requeryOperations.deleteAll(BasicUser.class);
    }

    @Test
    public void insert_user_without_association() throws Exception {
        BasicUser user = RandomData.randomUser();
        requeryOperations.insert(user);

        BasicUser loaded = requeryOperations.findById(BasicUser.class, user.getId());

        assertThat(loaded.getLastModifiedDate()).isNull();
        loaded.setName("updated");
        requeryOperations.update(loaded);

        assertThat(loaded.getLastModifiedDate()).isNotNull();

        // loaded2 is cached value
        BasicUser loaded2 = requeryOperations.findById(BasicUser.class, user.getId());
        assertThat(loaded2).isNotNull();
        assertThat(loaded2).isEqualTo(loaded);
    }

    @Test
    public void select_with_limit() throws Exception {

        BasicUser user = RandomData.randomUser();
        requeryOperations.insert(user);

        // NOTE: 특정 컬럼만 가지고 온 후, 다른 컬럼을 참조하면, Lazy loading을 수행해준다.
        Result<BasicUser> result = requeryOperations
            .select(BasicUser.class, BasicUser.ID, BasicUser.NAME)
            .limit(10)
            .get();

        BasicUser first = result.first();
        assertThat(first.getId()).isEqualTo(user.getId());
        assertThat(first.getName()).isEqualTo(user.getName());

        // NOTE: Lazy loading을 수행합니다 !!!
        assertThat(first.getAge()).isEqualTo(user.getAge());


        Result<BasicUser> result2 = requeryOperations
            .select(BasicUser.class, BasicUser.ID, BasicUser.NAME)
            .limit(10)
            .get();

        BasicUser first2 = result.first();
        assertThat(first2.getId()).isEqualTo(user.getId());
        assertThat(first2.getName()).isEqualTo(user.getName());

        // NOTE: Lazy loading을 수행합니다 !!!
        assertThat(first2.getAge()).isEqualTo(user.getAge());
    }
}
