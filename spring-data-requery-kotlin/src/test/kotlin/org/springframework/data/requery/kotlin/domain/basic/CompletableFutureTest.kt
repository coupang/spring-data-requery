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

import io.requery.async.KotlinCompletableEntityStore
import io.requery.kotlin.asc
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.RandomData
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import kotlin.test.assertTrue

/**
 * org.springframework.data.requery.kotlin.domain.basic.CompletableFutureTest
 *
 * @author debop
 */
class CompletableFutureTest : AbstractDomainTest() {

    private lateinit var asyncEntityStore: KotlinCompletableEntityStore<Any>

    @Before
    fun setup() {

        asyncEntityStore = KotlinCompletableEntityStore(kotlinDataStore, ForkJoinPool.commonPool())

        operations.deleteAll(BasicLocationEntity::class)
        operations.deleteAll(BasicGroupEntity::class)
        operations.deleteAll(BasicUserEntity::class)
    }

    @Test
    fun `asynchronous insert`() {
        val user = RandomData.randomBasicUser()

        with(asyncEntityStore) {
            insert(user)
                .thenAccept { savedUser ->
                    assertThat(savedUser).isNotNull

                    val loaded = select(BasicUserEntity::class)
                        .where(BasicUserEntity.ID.eq(user.id))
                        .get()
                        .first()
                    assertThat(loaded).isEqualTo(user)
                }
                .join()
        }
    }

    @Test
    fun `async insert and count`() {

        val user = RandomData.randomBasicUser()

        with(asyncEntityStore) {
            val rowCount = insert(user)
                .thenAccept { assertThat(it.id).isNotNull() }
                .thenCompose { count(BasicUserEntity::class).get().toCompletableFuture() }
                .get()
            assertTrue { rowCount > 0 }
        }
    }

    @Test
    fun `async insert one to many`() {

        val user = RandomData.randomBasicUser()

        with(asyncEntityStore) {
            insert(user)
                .thenApply { savedUser ->
                    val group = BasicGroupEntity().apply { name = "group" }
                    group.members.add(savedUser)
                    group
                }
                .thenCompose { group -> insert(group) }
                .get()
        }

        assertThat(user.groups).hasSize(1)
    }

    @Test
    fun `async query update`() {

        val user = RandomData.randomBasicUser().apply { age = 100 }

        with(asyncEntityStore) {
            insert(user)
                .thenCompose { _ ->
                    update<BasicUserEntity>(BasicUserEntity::class)
                        .set<String>(BasicUserEntity.ABOUT, "nothing")
                        .set<Int>(BasicUserEntity.AGE, 50)
                        .where(BasicUserEntity.AGE eq user.age)
                        .get()
                        .toCompletableFuture(Executors.newSingleThreadExecutor())
                }
                .thenApplyAsync { rowCount -> assertThat(rowCount).isEqualTo(1) }
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
    fun `query returns stream`() {

        val users = RandomData.randomBasicUsers(10)

        asyncEntityStore.insert<BasicUser>(users).join()

        val userStream = asyncEntityStore
            .select(BasicUser::class)
            .orderBy(BasicUser::name.asc())
            .limit(200)
            .get()
            .stream()

        assertThat(userStream.count()).isEqualTo(10L)
    }
}