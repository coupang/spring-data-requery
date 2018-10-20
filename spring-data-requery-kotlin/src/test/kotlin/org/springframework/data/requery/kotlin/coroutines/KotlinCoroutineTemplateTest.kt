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

package org.springframework.data.requery.kotlin.coroutines

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import mu.KLogging
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.RandomData
import org.springframework.data.requery.kotlin.domain.RandomData.randomBasicUser
import org.springframework.data.requery.kotlin.domain.basic.BasicGroup
import org.springframework.data.requery.kotlin.domain.basic.BasicLocation
import org.springframework.data.requery.kotlin.domain.basic.BasicUser
import org.springframework.data.requery.kotlin.domain.basic.BasicUserEntity
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KotlinCoroutineTemplateTest : AbstractDomainTest() {

    companion object : KLogging()

    @Autowired
    private lateinit var coroutineEntityStore: CoroutineEntityStore<Any>

    private val coroutineTemplate by lazy { CoroutineRequeryTemplate(coroutineEntityStore) }

    @Before
    fun setup() {
        runBlocking {
            with(coroutineTemplate) {
                val work1 = RequeryScope.async { deleteAll(BasicLocation::class) }
                val work2 = RequeryScope.async { deleteAll(BasicGroup::class) }
                val work3 = RequeryScope.async { deleteAll(BasicUser::class) }

                work1.await()
                work2.await()
                work3.await()
                logger.debug { "Complete to delete entities." }
            }
        }
    }

    @Test
    fun `async insert`() = runBlocking {
        val user = randomBasicUser()
        with(coroutineTemplate) {
            val savedUser = insert(user)

            val loaded =
                select(BasicUser::class)
                    .where(BasicUserEntity.ID.eq(savedUser.id))
                    .get()
                    .firstOrNull()

            assertEquals(user, loaded)
        }
    }

    @Test
    fun `async insert and count`() = runBlocking {
        with(coroutineTemplate) {
            val user = randomBasicUser()

            insert(user)
            assertNotNull(user.id)

            val count = count(BasicUser::class).get().value()
            assertEquals(1, count)
        }
    }

    @Test
    fun `async insert one to many`() = runBlocking {
        with(coroutineTemplate) {
            val user = randomBasicUser()

            assertNotNull(insert(user).id)

            val group = RandomData.randomBasicGroup()
            group.members.add(user)

            insert(group)

            assertEquals(1, user.groups.size)
            assertEquals(1, group.members.size)
        }
    }

    @Test
    fun `async query udpate`() = runBlocking<Unit> {
        val user = randomBasicUser().apply { age = 100 }

        with(coroutineTemplate) {
            val insert1 = async { insert(user) }
            val insert2 = async { insert(randomBasicUser().apply { age = 10 }) }

            insert1.await()
            insert2.await()

            val affectedCount = async {
                update(BasicUser::class)
                    .set(BasicUserEntity.ABOUT, "nothing")
                    .set(BasicUserEntity.AGE, 50)
                    .where(BasicUserEntity.AGE eq user.age)
                    .get()
                    .value()
            }

            val affectedCount2 = async {
                update(BasicUser::class)
                    .set(BasicUserEntity.ABOUT, "twenty")
                    .set(BasicUserEntity.AGE, 20)
                    .where(BasicUserEntity.AGE eq 10)
                    .get()
                    .value()
            }
            assertEquals(1, affectedCount.await())
            assertEquals(1, affectedCount2.await())
        }
    }

    @Test
    fun `query stream`() = runBlocking {
        val users = RandomData.randomBasicUsers(100)

        with(coroutineTemplate) {
            insertAll(users)

            val loadedUsers =
                select(BasicUser::class)
                    .orderBy(BasicUserEntity.NAME.asc().nullsFirst())
                    .limit(200)
                    .get()
                    .stream()

            assertEquals(100L, loadedUsers.count())
        }
    }
}