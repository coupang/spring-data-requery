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

import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.coroutines.CoroutineEntityStore
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.RandomData
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoroutineEntityStoreTest : AbstractDomainTest() {

    private val corStore: CoroutineEntityStore<Any> by lazy {
        CoroutineEntityStore(kotlinDataStore)
    }

    @Before
    fun setup() {
        runBlocking(Dispatchers.Default) {
            corStore.delete<BasicLocationEntity>(BasicLocationEntity::class).get().await()
            corStore.delete<BasicGroupEntity>(BasicGroupEntity::class).get().await()
            corStore.delete<BasicUserEntity>(BasicUserEntity::class).get().await()
        }
    }

    @Test
    fun `coroutine insert`() {
        runBlocking {
            val user = RandomData.randomBasicUser()

            corStore.insert(user).await()

            val result = corStore
                .select(BasicUserEntity::class)
                .where(BasicUserEntity.ID eq user.id)
                .get()

            val loaded = result.await { firstOrNull() }

            assertEquals(user, loaded)
        }
    }

    @Test
    fun `coroutine insert and count`() {
        runBlocking {

            with(corStore) {
                val deferredUser1 = insert(RandomData.randomBasicUser())
                val deferredUser2 = insert(RandomData.randomBasicUser())

                val user1 = deferredUser1.await()
                val user2 = deferredUser2.await()
                assertTrue { user1.id > 0 }
                assertTrue { user2.id > 0 }

                val rowCount = count(BasicUserEntity::class).get().await()
                assertTrue { rowCount > 0 }
            }
        }
    }

    @Test
    fun `insert one to many`() {
        runBlocking {

            val user = RandomData.randomBasicUser()
            corStore.insert(user).await()

            val group = BasicGroupEntity().apply { name = "group" }
            group.members.add(user)
            corStore.insert(group).await()

            assertEquals(1, user.groups.size)
        }
    }

    @Test
    fun `update query`() {
        runBlocking {
            val user = RandomData.randomBasicUser().apply { age = 100 }

            with(corStore) {
                insert(user).await()
                val affectedRow = update<BasicUserEntity>(BasicUserEntity::class)
                    .set<String>(BasicUserEntity.ABOUT, "nothing")
                    .set<Int>(BasicUserEntity.AGE, 50)
                    .where(BasicUserEntity.AGE eq user.age)
                    .get()
                    .await()

                assertEquals(1, affectedRow)
            }
        }
    }

    @Test
    fun `coroutine to blocking`() {
        val user = RandomData.randomBasicUser()

        corStore.toBlocking().insert(user)
        assertTrue { user.id > 0 }
    }

    @Test
    fun `query returns stream`() {
        runBlocking {
            val users = RandomData.randomBasicUsers(10)

            corStore.insert<BasicUserEntity>(users).await()
            assertTrue { users.all { it.id > 0 } }

            val userStream = corStore
                .select(BasicUserEntity::class)
                .orderBy(BasicUserEntity.NAME.asc())
                .limit(200)
                .get()
                .await { stream() }

            assertEquals(10, userStream.count())
        }
    }
}