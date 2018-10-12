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

package org.springframework.data.requery.kotlin.repository

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.requery.kotlin.configs.RequeryTestConfiguration
import org.springframework.data.requery.kotlin.domain.sample.RoleEntity
import org.springframework.data.requery.kotlin.repository.config.EnableRequeryRepositories
import org.springframework.data.requery.kotlin.repository.sample.RoleRepository
import org.springframework.data.requery.kotlin.repository.support.SimpleRequeryRepository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * org.springframework.data.requery.kotlin.repository.RoleRepositoryTest
 *
 * @author debop
 */
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [RequeryTestConfiguration::class])
@Transactional
@EnableRequeryRepositories(basePackageClasses = [RoleRepository::class])
class RoleRepositoryTest {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    @Autowired
    lateinit var repository: RoleRepository

    @Test
    fun `instancing repository`() {
        assertThat(repository).isNotNull

        assertThat(AopUtils.getTargetClass(repository)).isEqualTo(SimpleRequeryRepository::class.java)
    }

    @Test
    fun `create role`() {

        val reference = RoleEntity().apply { name = "ADMIN" }
        val result = repository.save(reference)

        assertThat(result).isEqualTo(reference)
    }

    @Test
    fun `update role`() {

        val reference = RoleEntity().apply { name = "ADMIN" }
        val result = repository.save(reference)

        assertThat(result).isEqualTo(reference)

        reference.name = "USER"
        repository.save(reference)

        assertThat(repository.findById(result.id!!)).isEqualTo(Optional.of(reference))
    }

    @Test
    fun `should use implicit count query`() {

        repository.deleteAll()

        val reference = RoleEntity().apply { name = "ADMIN" }
        repository.save(reference)

        assertThat(repository.count()).isEqualTo(1L)
    }

    @Test
    fun `should use implicit exists queries`() {

        val reference = RoleEntity().apply { name = "ADMIN" }
        repository.save(reference)

        assertThat(repository.existsById(reference.id!!)).isTrue()
    }

    @Test
    fun `should use explicitly configured entity name in derived count query`() {

        repository.deleteAll()

        val reference = RoleEntity().apply { name = "ADMIN" }
        repository.save(reference)

        assertThat(repository.countByName(reference.name)).isEqualTo(1L)
    }
}