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

package org.springframework.data.requery.kotlin.domain.basic

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.RandomData

class BasicEntityTest : AbstractDomainTest() {

    @Before
    fun setup() {
        operations.deleteAll(BasicLocationEntity::class)
        operations.deleteAll(BasicGroupEntity::class)
        operations.deleteAll(BasicUserEntity::class)
    }

    @Test
    fun `insert user without association`() {

        val user = RandomData.randomBasicUser()
        operations.insert(user)

        val loaded = operations.findById(BasicUserEntity::class, user.id)!!

        loaded.name = "updated"
        operations.update(loaded)

        assertThat(loaded.name).isEqualTo("updated")
    }

    @Test
    fun `select specific columns with limit`() {
        val user = RandomData.randomBasicUser()
        operations.insert(user)

        val result = operations
            .select(BasicUserEntity::class, BasicUserEntity.ID, BasicUserEntity.NAME)
            .limit(10)
            .get()

        val first = result.firstOrNull()
        assertThat(first).isNotNull
        assertThat(first.id).isEqualTo(user.id)
        assertThat(first.name).isEqualTo(user.name)

        // NOTE: Lazy loading 을 수행합니다.
        assertThat(first.age).isEqualTo(user.age)
    }
}