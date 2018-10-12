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

import io.reactivex.Completable
import io.reactivex.FlowableSubscriber
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.requery.kotlin.eq
import io.requery.reactivex.KotlinReactiveEntityStore
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import org.reactivestreams.Subscription
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.RandomData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * org.springframework.data.requery.kotlin.domain.basic.ReactiveTest
 *
 * @author debop
 */
class ReactiveTest : AbstractDomainTest() {

    private lateinit var reactiveStore: KotlinReactiveEntityStore<Any>

    @Before
    fun setup() {
        reactiveStore = KotlinReactiveEntityStore(kotlinDataStore)

        operations.deleteAll(BasicLocationEntity::class)
        operations.deleteAll(BasicGroupEntity::class)
        operations.deleteAll(BasicUserEntity::class)
    }

    @Test
    fun `reactive insert`() {

        val user = RandomData.randomBasicUser()
        val latch = CountDownLatch(1)

        reactiveStore.insert(user)
            .subscribe { saved ->
                assertThat(saved.id).isNotNull()

                val loaded = reactiveStore.select(BasicUserEntity::class)
                    .where(BasicUserEntity.ID.eq(saved.id))
                    .get()
                    .firstOrNull()!!
                assertThat(loaded).isEqualTo(saved)
                latch.countDown()
            }

        latch.await()
    }

    @Test
    fun `reactive delete`() {

        val user = RandomData.randomBasicUser()

        reactiveStore.insert(user).blockingGet()
        assertThat(user.id).isNotNull()

        reactiveStore.delete(user).blockingGet()

        val saved = reactiveStore
            .select(BasicUserEntity::class)
            .where(BasicUser::id eq user.id)
            .get()
            .firstOrNull()

        assertThat(saved).isNull()
    }

    @Test
    fun `insert and get count`() {

        val user = RandomData.randomBasicUser()

        Observable.just(user)
            .concatMap { reactiveStore.insert(it).toObservable() }

        val saved = reactiveStore.insert(user).blockingGet()
        assertThat(saved).isNotNull

        val count = reactiveStore.count(BasicUserEntity::class).get().single().blockingGet()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `insert one to many`() {

        val user = RandomData.randomBasicUser()

        reactiveStore
            .insert(user)
            .map {
                val group = BasicGroupEntity()
                group.members.add(it)
                group
            }
            .map { operations.insert(it) }
            .blockingGet()

        assertThat(user.groups).hasSize(1)
    }

    @Test
    fun `query empty entity`() {
        val latch = CountDownLatch(1)

        reactiveStore.select(BasicUserEntity::class).get()
            .observable()
            .observeOn(Schedulers.io())
            .subscribe(
                { _ -> fail("User가 없어야 합니다.") },
                { _ -> fail("예외도 나서는 안됩니다.") },
                latch::countDown
            )

        if(!latch.await(1000, TimeUnit.MILLISECONDS)) {
            fail("Timeout 이 나야 정상임")
        }
    }

    @Test
    fun `query observable`() {

        val users = RandomData.randomBasicUsers(30)
        val userCount = users.size

        reactiveStore.insert<BasicUser>(users).subscribeOn(Schedulers.io()).blockingGet()

        val rowCount = AtomicInteger()

        reactiveStore.select(BasicUser::class).limit(userCount * 2).get()
            .observable()
            .observeOn(Schedulers.computation())
            .subscribe { user ->
                assertThat(user.id).isNotNull()
                rowCount.incrementAndGet()
            }

        Thread.sleep(10)
        assertThat(rowCount.get()).isEqualTo(userCount)
    }

    @Test
    fun `query self observable map`() {
        val count = AtomicInteger(0)

        val disposable = reactiveStore
            .select(BasicUserEntity::class)
            .limit(2)
            .get()
            .observableResult()
            .flatMap { it.observable() }
            .subscribe { count.incrementAndGet() }

        reactiveStore.insert(RandomData.randomBasicUser()).blockingGet()
        reactiveStore.insert(RandomData.randomBasicUser()).blockingGet()

        assertThat(count.get()).isEqualTo(3)
        disposable.dispose()
    }

    @Test
    fun `self observable delete query`() {

        val count = AtomicInteger()

        val disposable = reactiveStore
            .select(BasicUserEntity::class)
            .get()
            .observableResult()
            .subscribe { count.incrementAndGet() }

        val user = RandomData.randomBasicUser()

        reactiveStore.insert(user).blockingGet()
        assertThat(count.get()).isEqualTo(2)

        val rows = reactiveStore.delete<BasicUser>(BasicUser::class).get().value()

        assertThat(count.get()).isEqualTo(3)
        assertThat(rows).isEqualTo(1)
        disposable.dispose()
    }

    @Test
    fun `query self observable relational`() {

        val count = AtomicInteger()

        val disposable = reactiveStore
            .select(BasicUserEntity::class)
            .get()
            .observableResult()
            .subscribe { count.incrementAndGet() }

        val user = RandomData.randomBasicUser()

        reactiveStore.insert(user).blockingGet()
        assertThat(count.get()).isEqualTo(2)

        val group = RandomData.randomBasicGroup()
        group.members.add(user)
        user.about = "new about"

        reactiveStore.update(user).blockingGet()
        assertThat(count.get()).isEqualTo(3)

        reactiveStore.delete(user).blockingGet()
        assertThat(count.get()).isEqualTo(4)

        disposable.dispose()
    }

    @Test
    fun `query observable from entity`() {

        val user = RandomData.randomBasicUser()

        reactiveStore
            .insert(user)
            .map { savedUser ->
                BasicGroupEntity().apply {
                    members.add(savedUser)
                }
            }
            .flatMap { reactiveStore.insert(it) }
            .blockingGet()

        assertThat(user.groups).hasSize(1)
    }

    @Test
    fun `run with transaction`() {

        val user = RandomData.randomBasicUser()

        reactiveStore.withTransaction {
            insert(user)
            user.about = "new about"
            update(user)

            delete(user)
        }

        assertThat(reactiveStore.count(BasicUserEntity::class).get().value()).isEqualTo(0)

        val user2 = RandomData.randomBasicUser()

        reactiveStore.withTransaction {
            insert(user2)
        }.blockingGet()

        assertThat(reactiveStore.count(BasicUserEntity::class).get().value()).isEqualTo(1)
    }

    @Test
    fun `run with transaction from blocking`() {

        val blocking = reactiveStore.toBlocking()

        Completable.fromCallable {
            blocking.withTransaction {
                val user = RandomData.randomBasicUser()
                insert(user)

                user.about = "new about"
                update(user)
            }
        }.subscribe()

        assertThat(reactiveStore.count(BasicUserEntity::class).get().value()).isEqualTo(1)
    }

    @Test
    fun `query observable pull`() {

        val COUNT = 30
        val users = RandomData.randomBasicUsers(COUNT)
        reactiveStore.insert<BasicUser>(users).blockingGet()

        val loadedUsers = mutableListOf<BasicUser>()

        reactiveStore.select(BasicUser::class)
            .get()
            .flowable()
            .subscribeOn(Schedulers.trampoline())
            .subscribe(object : FlowableSubscriber<BasicUser> {
                private lateinit var subscription: Subscription
                override fun onSubscribe(s: Subscription) {
                    this.subscription = s
                    this.subscription.request(10)
                }

                override fun onNext(basicUser: BasicUser) {
                    loadedUsers.add(basicUser)
                    if(loadedUsers.size % 10 == 0 && loadedUsers.size > 1) {
                        subscription.request(10)
                    }
                }

                override fun onError(t: Throwable?) {}
                override fun onComplete() {}
            })

        assertThat(loadedUsers.size).isEqualTo(COUNT)
    }
}