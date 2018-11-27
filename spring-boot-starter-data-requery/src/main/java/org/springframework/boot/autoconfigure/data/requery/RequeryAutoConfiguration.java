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

package org.springframework.boot.autoconfigure.data.requery;

import io.requery.cache.WeakEntityCache;
import io.requery.meta.EntityModel;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.listeners.LogbackListener;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.Field;

/**
 * org.springframework.boot.autoconfigure.data.requery.RequeryAutoConfiguration
 *
 * @author debop
 */
@Slf4j
@Configuration
@ConditionalOnBean({ DataSource.class })
@EnableConfigurationProperties(RequeryProperties.class)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class })
public class RequeryAutoConfiguration {

    @Nonnull
    private final RequeryProperties properties;

    public RequeryAutoConfiguration(@Nonnull final RequeryProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityModel entityModel() {
        try {
            String modelFullName = properties.getModelName();
            if (StringUtils.isEmpty(modelFullName)) {
                throw new IllegalArgumentException("Not provide spring.data.requery.modelName property");
            }
            String className = StringUtils.stripFilenameExtension(modelFullName);
            String modelName = StringUtils.getFilenameExtension(modelFullName);
            log.debug("Entity model name={}", modelFullName);

            Class<?> clazz = ClassUtils.forName(className, Thread.currentThread().getContextClassLoader());

            Field field = clazz.getField(modelName);
            return (EntityModel) field.get(null);

        } catch (Exception e) {
            throw new IllegalArgumentException("Not found model name. model name=" + properties.getModelName());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ DataSource.class, EntityModel.class })
    public io.requery.sql.Configuration requeryConfiguration(@Nonnull final DataSource dataSource,
                                                             @Nonnull final EntityModel entityModel) {
        return new ConfigurationBuilder(dataSource, entityModel)
            .setStatementCacheSize(properties.getStatementCacheSize())
            .setBatchUpdateSize(properties.getBatchUpdateSize())
            .setEntityCache(new WeakEntityCache())
            .addStatementListener(new LogbackListener<>())
            .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ io.requery.sql.Configuration.class })
    public EntityDataStore<Object> entityDataStore(@Nonnull final io.requery.sql.Configuration configuration) {
        return new EntityDataStore<>(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ DataSource.class })
    public PlatformTransactionManager transactionManager(@Nonnull final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Autowired io.requery.sql.Configuration configuration;

    /**
     * 사용할 Database에 Requery Entity에 해당하는 Schema 를 생성하는 작업을 수행합니다.
     */
    @PostConstruct
    protected void setupSchema() {
        if (properties.getTableCreationMode() == null) {
            log.info("No action to Database schema");
            return;
        }
        log.info("Setup Requery Database Schema... mode={}", properties.getTableCreationMode());

        try {
            SchemaModifier schema = new SchemaModifier(configuration);
            log.debug(schema.createTablesString(properties.getTableCreationMode()));
            schema.createTables(properties.getTableCreationMode());
            log.info("Success to setup database schema!!!");
        } catch (Exception e) {
            log.error("Fail to setup database schema!!!", e);
            throw new RuntimeException(e);
        }
    }
}
