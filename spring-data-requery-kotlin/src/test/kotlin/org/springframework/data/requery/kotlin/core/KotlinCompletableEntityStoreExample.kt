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

package org.springframework.data.requery.kotlin.core

import io.requery.async.KotlinCompletableEntityStore
import io.requery.kotlin.eq
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.RandomData
import org.springframework.data.requery.kotlin.domain.basic.BasicGroup
import org.springframework.data.requery.kotlin.domain.basic.BasicGroupEntity
import org.springframework.data.requery.kotlin.domain.basic.BasicLocation
import org.springframework.data.requery.kotlin.domain.basic.BasicUser
import org.springframework.data.requery.kotlin.domain.basic.BasicUserEntity
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

/**
 * [KotlinCompletableEntityStore] 에 대한 예제 
 * @author debop
 * @since 18. 6. 2
 */
class KotlinCompletableEntityStoreExample : AbstractDomainTest() {

    companion object : KLogging()

    private val asyncEntityStore: KotlinCompletableEntityStore<Any> by lazy {
        KotlinCompletableEntityStore(kotlinDataStore, ForkJoinPool.commonPool())
    }

    @Before
    fun setup() {
        with(asyncEntityStore) {
            val task1 = delete<BasicLocation>(BasicLocation::class).get()
            val task2 = delete<BasicGroup>(BasicGroup::class).get()
            val task3 = delete<BasicUser>(BasicUser::class).get()

            task1.call()
            task2.call()
            task3.call()
        }
    }

    @Test
    fun `async insert`() {
        val user = RandomData.randomBasicUser()
        asyncEntityStore
            .insert(user)
            .thenAccept { u ->
                assertThat(u.id).isNotNull()
                val loaded = asyncEntityStore.select(BasicUser::class).where(BasicUser::id eq user.id).get().first()
                assertThat(loaded).isEqualTo(user)
            }.join()
    }

    @Test
    fun `async insert and count`() {

        with(asyncEntityStore) {
            val user = RandomData.randomBasicUser()

            insert(user)
                .thenAccept { savedUser ->
                    assertThat(savedUser.id).isNotNull()
                }
                .thenCompose {
                    count(BasicUser::class).get().toCompletableFuture()
                }
                .get()

            logger.debug { "Insert asynchronous, and get count" }
        }
    }

    @Test
    fun `async insert one to many`() {

        with(asyncEntityStore) {
            val user = RandomData.randomBasicUser()

            insert(user)
                .thenApply { savedUser ->
                    val group = BasicGroupEntity().apply { name = "group" }
                    group.members.add(savedUser)
                    group
                }
                .thenCompose { group ->
                    insert(group)
                }
                .get()

            assertThat(user.groups).hasSize(1)
        }
    }

    @Test
    fun `async query update`() {
        val user = RandomData.randomBasicUser().apply { age = 100 }

        with(asyncEntityStore) {
            insert(user)
                .thenCompose { _ ->
                    update<BasicUserEntity>(BasicUserEntity::class)
                        .set(BasicUserEntity.ABOUT, "nothing")
                        .set(BasicUserEntity.AGE, 50)
                        .where(BasicUserEntity.AGE eq user.age)
                        .get()
                        .toCompletableFuture(Executors.newSingleThreadExecutor())
                }.thenApply { rowCount ->
                    assertThat(rowCount).isEqualTo(1)
                }
                .get()
        }
    }

    @Test
    fun `async to blocking`() {
        val user = RandomData.randomBasicUser()
        asyncEntityStore.toBlocking().insert(user)
        assertThat(user.id).isNotNull()
    }

    @Test
    fun `query stream`() {
        val users = RandomData.randomBasicUsers(100)

        with(asyncEntityStore) {
            insert<BasicUser>(users).join()

            val loadedUsers =
                select(BasicUser::class)
                    .orderBy(BasicUserEntity.NAME.asc().nullsLast())
                    .limit(200)
                    .get()
                    .stream()
                    .map { user -> user }

            assertThat(loadedUsers.count()).isEqualTo(100L)
        }
    }
}