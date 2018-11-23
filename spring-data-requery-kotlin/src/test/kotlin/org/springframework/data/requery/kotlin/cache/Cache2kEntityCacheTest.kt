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

package org.springframework.data.requery.kotlin.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.RandomData
import org.springframework.data.requery.kotlin.domain.sample.Role
import org.springframework.data.requery.kotlin.domain.sample.RoleEntity
import org.springframework.data.requery.kotlin.domain.sample.User
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Cache2kEntityCacheTest {

    private val entityCache = Cache2kEntityCache()

    @Before
    fun setup() {
        entityCache.clear()
    }

    @Test
    fun `instancing Cache2kEntityCache`() {

        val user = RandomData.randomUser()
        val userId = 1

        assertFalse { entityCache.contains(User::class.java, userId) }

        entityCache.put(User::class.java, userId, user)
        assertTrue { entityCache.contains(User::class.java, userId) }

        val cached = entityCache.get(User::class.java, userId)

        assertNotNull(cached)
        assertEquals(user, cached)
    }

    @Test
    fun `invalidate cache item`() {

        val user = RandomData.randomUser()
        val userId = 1

        entityCache.put(User::class.java, userId, user)
        assertThat(entityCache.contains(User::class.java, userId)).isTrue()

        entityCache.invalidate(User::class.java, userId)
        assertThat(entityCache.contains(User::class.java, userId)).isFalse()
    }

    @Test
    fun `invalidate all`() {

        val user = RandomData.randomUser()
        val userId = 1

        val user2 = RandomData.randomUser()
        val user2Id = 2

        entityCache.put(User::class.java, userId, user)
        entityCache.put(User::class.java, user2Id, user2)
        assertThat(entityCache.contains(User::class.java, userId)).isTrue()
        assertThat(entityCache.contains(User::class.java, user2Id)).isTrue()

        entityCache.invalidate(User::class.java)
        assertThat(entityCache.contains(User::class.java, userId)).isFalse()
        assertThat(entityCache.contains(User::class.java, user2Id)).isFalse()
    }

    @Test
    fun `various entity type caching`() {

        val user = RandomData.randomUser()
        val userId = 1

        val role = RoleEntity()
        val roleId = 2

        entityCache.put(User::class.java, userId, user)
        assertThat(entityCache.contains(User::class.java, userId)).isTrue()

        entityCache.put(Role::class.java, roleId, role)
        assertThat(entityCache.contains(Role::class.java, roleId)).isTrue()


        entityCache.clear()
        assertThat(entityCache.contains(User::class.java, userId)).isFalse()
        assertThat(entityCache.contains(Role::class.java, roleId)).isFalse()
    }

    @Test
    fun `caching in multithread env`() = runBlocking {
        withContext(Dispatchers.Default) {

            val job1 = launch {
                val user = RandomData.randomUser()
                val userId = 42
                entityCache.put(User::class.java, userId, user)
                assertTrue { entityCache.contains(User::class.java, userId) }
            }

            val job2 = launch {
                val role = RoleEntity()
                val roleId = 43

                entityCache.put(Role::class.java, roleId, role)
                assertTrue { entityCache.contains(Role::class.java, roleId) }
            }

            job1.join()
            job2.join()
        }
    }
}