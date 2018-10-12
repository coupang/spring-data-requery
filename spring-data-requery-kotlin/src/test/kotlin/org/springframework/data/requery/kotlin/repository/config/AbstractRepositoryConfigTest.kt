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
import org.springframework.data.requery.kotlin.mapping.RequeryMappingContext
import org.springframework.data.requery.kotlin.repository.sample.AccountRepository
import org.springframework.data.requery.kotlin.repository.sample.RoleRepository
import org.springframework.data.requery.kotlin.repository.sample.UserRepository
import org.springframework.test.context.junit4.SpringRunner

/**
 * AbstractRepositoryConfigTest
 *
 * @author debop
 */
@RunWith(SpringRunner::class)
abstract class AbstractRepositoryConfigTest {

    @Autowired(required = false) lateinit var userRepository: UserRepository
    @Autowired(required = false) lateinit var roleRepository: RoleRepository
    @Autowired(required = false) lateinit var accountRepository: AccountRepository

    @Autowired lateinit var mappingContext: RequeryMappingContext

    @Test
    fun `context loading`() {
        assertThat(userRepository).isNotNull
        assertThat(roleRepository).isNotNull
        assertThat(accountRepository).isNotNull
    }

    @Test
    fun `repositories have exception translation applied`() {
        TODO("Not implemented")
    }
}