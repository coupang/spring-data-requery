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

package org.springframework.data.requery.kotlin.repository.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.requery.kotlin.repository.sample.UserRepository
import org.springframework.data.requery.kotlin.repository.support.RequeryRepositoryFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.util.ReflectionTestUtils

/**
 * org.springframework.data.requery.repository.config.QueryLookupStrategyTest
 *
 * @author debop
 */
@RunWith(SpringRunner::class)
@ContextConfiguration
class QueryLookupStrategyTest {

    @Configuration
    @EnableRequeryRepositories(
        basePackageClasses = [UserRepository::class],
        queryLookupStrategy = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND,
        includeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [UserRepository::class])]
    )
    class TestConfiguration : InfrastructureConfig()

    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun `should use explicitly configured QueryLookupStrategy`() {

        val factory = context.getBean("&userRepository", RequeryRepositoryFactoryBean::class.java)

        assertThat(ReflectionTestUtils.getField(factory, "queryLookupStrategyKey"))
            .isEqualTo(QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND)
    }
}