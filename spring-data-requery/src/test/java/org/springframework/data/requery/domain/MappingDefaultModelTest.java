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

package org.springframework.data.requery.domain;

import io.requery.sql.Configuration;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;
import io.requery.sql.TableCreationMode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.sample.Role;
import org.springframework.data.requery.listeners.LogbackListener;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Diego on 2018. 6. 12..
 */
@Slf4j
public class MappingDefaultModelTest {

    private DataSource dataSource;
    private Configuration configuration;
    private EntityDataStore<Object> dataStore;
    private RequeryOperations operations;

    @Before
    public void setup() {
        dataSource = new EmbeddedDatabaseBuilder()
            .setName("test")
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .build();

        configuration = new ConfigurationBuilder(dataSource, Models.DEFAULT)
            .addStatementListener(new LogbackListener<>())
            .build();

        dataStore = new EntityDataStore<>(configuration);

        SchemaModifier schemaModifier = new SchemaModifier(configuration);

        log.debug("{}", schemaModifier.createTablesString(TableCreationMode.CREATE_NOT_EXISTS));
        schemaModifier.createTables(TableCreationMode.CREATE_NOT_EXISTS);
    }

    @Test
    public void verify_mapping_entities() {
        Role role = new Role();
        role.setName("person");
        dataStore.insert(role);

        assertThat(role.getId()).isNotNull();
    }

}
