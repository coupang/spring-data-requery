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

package org.springframework.data.requery.kotlin.configs

import io.requery.cache.EmptyEntityCache
import io.requery.meta.EntityModel
import io.requery.sql.ConfigurationBuilder
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.SchemaModifier
import io.requery.sql.TableCreationMode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.core.RequeryTemplate
import org.springframework.data.requery.kotlin.core.RequeryTransactionManager
import org.springframework.data.requery.kotlin.listeners.LogbackListener
import org.springframework.data.requery.kotlin.mapping.RequeryMappingContext
import org.springframework.transaction.PlatformTransactionManager
import javax.annotation.PostConstruct
import javax.sql.DataSource

/**
 * Spring 용 Requery 환경설정 파일입니다.
 *
 * @author debop
 * @since 18. 7. 2
 */
@Configuration
abstract class AbstractRequeryConfiguration {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    @Bean
    abstract fun getEntityModel(): EntityModel

    fun getTableCreationMode(): TableCreationMode {
        return TableCreationMode.CREATE_NOT_EXISTS
    }

    @Bean
    fun requeryConfiguration(dataSource: DataSource, entityModel: EntityModel): io.requery.sql.Configuration {
        return ConfigurationBuilder(dataSource, entityModel)
            .setEntityCache(EmptyEntityCache())
            .setStatementCacheSize(1024)
            .setBatchUpdateSize(100)
            .addStatementListener(LogbackListener<Any>())
            .build()
    }

    @Bean(destroyMethod = "close")
    fun entityDataStore(configuration: io.requery.sql.Configuration): KotlinEntityDataStore<Any> {
        return KotlinEntityDataStore<Any>(configuration).apply {
            log.info { "Create Requery EntityDataStore instance." }
        }
    }

    @Bean
    fun requeryOperations(entityDataStore: KotlinEntityDataStore<Any>, mappingContext: RequeryMappingContext): RequeryOperations {
        log.info { "Create RequeryOperations instance." }
        return RequeryTemplate(entityDataStore, mappingContext)
    }

    @Bean
    fun requeryMappingContext(applicationContext: ApplicationContext): RequeryMappingContext {
        return RequeryMappingContext().apply {
            setApplicationContext(applicationContext)
        }
    }

    @Bean
    fun transactionManager(entityDataStore: KotlinEntityDataStore<Any>, dataSource: DataSource): PlatformTransactionManager {
        return RequeryTransactionManager(entityDataStore, dataSource)
    }

    @Autowired
    lateinit var configuration: io.requery.sql.Configuration

    /**
     * DB Schema 생성이 필요할 경우, Application 시작 시, 존재하지 않는 TABLE을 생성하도록 해줍니다.
     */
    @PostConstruct
    protected fun buildSchema() {
        log.info { "Create Database Schema ..." }
        try {
            val schema = SchemaModifier(configuration)
            log.debug { schema.createTablesString(getTableCreationMode()) }
            schema.createTables(getTableCreationMode())
            log.info { "Success to create database schema" }
        } catch(ignored: Exception) {
            log.error(ignored) { "Fail to creation database schema." }
        }
    }
}