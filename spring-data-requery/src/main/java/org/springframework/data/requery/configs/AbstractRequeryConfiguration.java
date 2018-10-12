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

package org.springframework.data.requery.configs;

import io.requery.cache.EmptyEntityCache;
import io.requery.meta.EntityModel;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;
import io.requery.sql.TableCreationMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.core.RequeryTemplate;
import org.springframework.data.requery.core.RequeryTransactionManager;
import org.springframework.data.requery.listeners.LogbackListener;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Spring 용 Requery 환경설정 파일입니다.
 *
 * @author debop
 * @since 18. 6. 4
 */
@Slf4j
@Configuration
public abstract class AbstractRequeryConfiguration {

    /**
     * Requery용 EntityModel 을 지정해주셔야 합니다. 기본적으로 Models.DEFAULT 를 지정해주시면 됩니다.
     */
    @Bean
    abstract public EntityModel getEntityModel();

    /**
     * Schema Generation 옵션입니다. 기본적으로 {@link TableCreationMode#CREATE_NOT_EXISTS} 을 사용합니다.
     *
     * @return {@link TableCreationMode} 값
     */
    public TableCreationMode getTableCreationMode() {
        return TableCreationMode.CREATE_NOT_EXISTS;
    }

    @Bean
    public io.requery.sql.Configuration requeryConfiguration(DataSource dataSource, EntityModel entityModel) {
        Assert.notNull(dataSource, "dataSource must not be null");
        Assert.notNull(getEntityModel(), "enittymodel must not be null");

        return new ConfigurationBuilder(dataSource, entityModel)
            // .useDefaultLogging()
            .setEntityCache(new EmptyEntityCache())
            .setStatementCacheSize(1024)
            .setBatchUpdateSize(100)
            .addStatementListener(new LogbackListener<>())
            .build();
    }

    @Bean(destroyMethod = "close")
    public EntityDataStore<Object> entityDataStore(io.requery.sql.Configuration configuration) {
        log.info("Create EntityDataStore instance.");
        return new EntityDataStore<>(configuration);
    }

    @Bean
    public RequeryOperations requeryOperations(EntityDataStore<Object> entityDataStore, RequeryMappingContext mappingContext) {
        log.info("Create RequeryTemplate instance.");
        return new RequeryTemplate(entityDataStore, mappingContext);
    }

    // TODO: 꼭 필요한 Class 인지 다시 판단해보자.
    @Bean
    public RequeryMappingContext requeryMappingContext(ApplicationContext applicationContext) {
        RequeryMappingContext context = new RequeryMappingContext();
        context.setApplicationContext(applicationContext);
        return context;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityDataStore<Object> entityDataStore, DataSource dataSource) {
        return new RequeryTransactionManager(entityDataStore, dataSource);
    }


    @Autowired io.requery.sql.Configuration configuration;

    /**
     * 사용할 Database에 Requery Entity에 해당하는 Schema 를 생성하는 작업을 수행합니다.
     */
    @PostConstruct
    protected void setupSchema() {
        log.info("Setup Requery Database Schema... mode={}", getTableCreationMode());

        try {
            SchemaModifier schema = new SchemaModifier(configuration);
            log.debug(schema.createTablesString(getTableCreationMode()));
            schema.createTables(getTableCreationMode());
            log.info("Success to setup database schema!!!");
        } catch (Exception e) {
            log.error("Fail to setup database schema!!!", e);
            throw new RuntimeException(e);
        }
    }
}
