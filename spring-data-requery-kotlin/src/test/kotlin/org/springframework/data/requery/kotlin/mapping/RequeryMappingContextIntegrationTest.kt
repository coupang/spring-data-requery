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

package org.springframework.data.requery.kotlin.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.requery.kotlin.configs.RequeryTestConfiguration
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.domain.sample.User
import org.springframework.data.requery.kotlin.repository.config.EnableRequeryRepositories
import org.springframework.data.requery.kotlin.repository.sample.UserRepository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@Ignore("현재 Spring Framework에서 무한루프가 발생한다.")
@RunWith(SpringRunner::class)
@ContextConfiguration
class RequeryMappingContextIntegrationTest {

    @Configuration
    @EnableRequeryRepositories(
        basePackageClasses = [UserRepository::class],
        includeFilters = [ComponentScan.Filter(value = [UserRepository::class], type = FilterType.ASSIGNABLE_TYPE)]
    )
    class TestConfig : RequeryTestConfiguration()

    lateinit var context: RequeryMappingContext

    @Autowired lateinit var users: UserRepository
    @Autowired lateinit var operations: RequeryOperations

    @Before
    fun setup() {
        context = RequeryMappingContext()
    }

    @Test
    fun `setup mapping context correctly`() {

        val entity = context.getRequiredPersistentEntity(User::class.java)
        assertThat(entity).isNotNull
    }
}