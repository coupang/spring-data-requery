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

package org.springframework.data.requery.domain.basic;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.requery.BlockingEntityStore;
import io.requery.reactivex.ReactiveEntityStore;
import io.requery.reactivex.ReactiveSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.RandomData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * @author Diego on 2018. 6. 12..
 */
@Slf4j
public class ReactiveTest extends AbstractDomainTest {

    private ReactiveEntityStore<Object> reactiveStore;

    @Before
    public void setup() {
        reactiveStore = ReactiveSupport.toReactiveStore(dataStore);
        requeryTemplate.deleteAll(BasicGroup.class);
        requeryTemplate.deleteAll(BasicLocation.class);
        requeryTemplate.deleteAll(BasicUser.class);
    }

    @Test
    public void reactive_insert() throws Exception {
        BasicUser user = RandomData.randomUser();
        CountDownLatch latch = new CountDownLatch(1);

        reactiveStore.insert(user)
            .subscribe(saved -> {
                assertThat(saved.getId()).isNotNull();
                BasicUser loaded = reactiveStore.select(BasicUser.class)
                    .where(BasicUser.ID.eq(saved.getId()))
                    .get()
                    .first();
                assertThat(loaded).isEqualTo(saved);
                latch.countDown();
            });

        latch.await();
    }

    @Test
    public void reactive_delete() {
        BasicUser user = RandomData.randomUser();

        reactiveStore.insert(user).blockingGet();
        assertThat(user.getId()).isNotNull();

        reactiveStore.delete(user).blockingGet();

        BasicUser loaded = reactiveStore.select(BasicUser.class)
            .where(BasicUser.ID.eq(user.getId()))
            .get()
            .firstOrNull();

        assertThat(loaded).isNull();
    }

    @Test
    public void insert_and_get_count() {
        BasicUser user = RandomData.randomUser();

        Observable.just(user)
            .concatMap(u -> reactiveStore.insert(u).toObservable());

        BasicUser saved = reactiveStore.insert(user).blockingGet();
        assertThat(saved).isNotNull();

        int count = reactiveStore.count(BasicUser.class).get().single().blockingGet();
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void insert_one_to_many() {
        BasicUser user = RandomData.randomUser();
        reactiveStore.insert(user).map(it -> {
            BasicGroup group = new BasicGroup();
            group.getMembers().add(it);
            return group;
        }).map(group -> requeryTemplate.insert(group)).blockingGet();

        assertThat(user.getGroups()).hasSize(1);
    }

    @Test
    public void query_empty_entity() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        reactiveStore.select(BasicUser.class).get().observable()
            .observeOn(Schedulers.io())
            .subscribe(next -> fail("User가 없어야 하는뎅"),
                       error -> fail("예외도 나서는 안되는뎅"),
                       latch::countDown);

        if (!latch.await(1, TimeUnit.SECONDS)) {
            fail("Timeout이 나야 정상임");
        }
    }

    @Test
    public void query_observable() throws Exception {
        Set<BasicUser> users = RandomData.randomUsers(30);
        int userCount = users.size();
        reactiveStore.insert(users).subscribeOn(Schedulers.io()).blockingGet();

        AtomicInteger rowCount = new AtomicInteger();
        reactiveStore.select(BasicUser.class).limit(50).get()
            .observable()
            .observeOn(Schedulers.computation())
            .subscribe(user -> {
                assertThat(user.getId()).isNotNull();
                rowCount.incrementAndGet();
            });

        Thread.sleep(10);
        assertThat(rowCount.get()).isEqualTo(userCount);
    }

    @Test
    public void query_self_observable_map() {
        AtomicInteger count = new AtomicInteger(0);

        Disposable disposable = reactiveStore.select(BasicUser.class).limit(2).get().observableResult()
            .flatMap(user -> user.observable())
            .subscribe(it -> count.incrementAndGet());

        reactiveStore.insert(RandomData.randomUser()).blockingGet();
        reactiveStore.insert(RandomData.randomUser()).blockingGet();

        assertThat(count.get()).isEqualTo(3);
        disposable.dispose();
    }

    @Test
    public void self_observable_delete() {
        AtomicInteger count = new AtomicInteger();

        Disposable disposable = reactiveStore.select(BasicUser.class).get().observableResult()
            .subscribe(it -> count.incrementAndGet());

        BasicUser user = RandomData.randomUser();

        reactiveStore.insert(user).blockingGet();
        reactiveStore.delete(user).blockingGet();

        assertThat(count.get()).isEqualTo(3);
        disposable.dispose();
    }

    @Test
    public void self_observable_delete_query() {
        AtomicInteger count = new AtomicInteger();

        Disposable disposable = reactiveStore.select(BasicUser.class).get().observableResult()
            .subscribe(it -> count.incrementAndGet());

        BasicUser user = RandomData.randomUser();

        reactiveStore.insert(user).blockingGet();
        assertThat(count.get()).isEqualTo(2);

        int rows = reactiveStore.delete(BasicUser.class).get().value();
        assertThat(count.get()).isEqualTo(3);
        assertThat(rows).isEqualTo(1);
        disposable.dispose();
    }

    @Test
    public void query_self_observable_relational() {
        AtomicInteger count = new AtomicInteger();

        Disposable disposable = reactiveStore.select(BasicUser.class).get().observableResult()
            .subscribe(it -> count.incrementAndGet());

        BasicUser user = RandomData.randomUser();

        reactiveStore.insert(user).blockingGet();
        assertThat(count.get()).isEqualTo(2);

        BasicGroup group = RandomData.randomBasicGroup();
        user.getGroups().add(group);
        user.setAbout("new about");
        reactiveStore.update(user).blockingGet();
        assertThat(count.get()).isEqualTo(3);

        reactiveStore.delete(user).blockingGet();
        assertThat(count.get()).isEqualTo(4);

        disposable.dispose();
    }

    @Test
    public void query_observable_from_entity() {
        BasicUser user = RandomData.randomUser();

        reactiveStore.insert(user)
            .map(it -> {
                BasicGroup group = new BasicGroup();
                group.getMembers().add(it);
                return group;
            }).flatMap(group -> reactiveStore.insert(group))
            .blockingGet();

        assertThat(user.getGroups()).hasSize(1);
    }

    @Test
    public void run_in_transaction() {
        BasicUser user = RandomData.randomUser();

        reactiveStore.runInTransaction(entityStore -> {
            entityStore.insert(user);
            user.setAbout("new about");
            entityStore.update(user);
            return entityStore.delete(user);
        }); // .blockingGet() NullPointerException

        assertThat(reactiveStore.count(BasicUser.class).get().value()).isEqualTo(0);

        BasicUser user2 = RandomData.randomUser();

        reactiveStore.runInTransaction(entityStore -> entityStore.insert(user2)).blockingGet();

        assertThat(reactiveStore.count(BasicUser.class).get().value()).isEqualTo(1);
    }

    @Test
    public void run_in_transaction_from_blocking() {
        BlockingEntityStore<Object> blocking = reactiveStore.toBlocking();
        Completable.fromCallable(
            () -> blocking.runInTransaction(() -> {
                BasicUser user = RandomData.randomUser();
                blocking.insert(user);

                user.setAbout("new about");
                return blocking.update(user);
            })).subscribe();

        assertThat(reactiveStore.count(BasicUser.class).get().value()).isEqualTo(1);
    }

    @Test
    public void query_observable_pull() {
        Set<BasicUser> users = RandomData.randomUsers(100);
        int userCount = users.size();
        reactiveStore.insert(users, BasicUser.class).blockingGet();

        List<BasicUser> loadedUsers = new ArrayList<>();

        reactiveStore.select(BasicUser.class)
            .get()
            .flowable()
            .subscribeOn(Schedulers.trampoline())
            .subscribe(
                new Subscriber<BasicUser>() {
                    Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        subscription.request(10);
                    }

                    @Override
                    public void onNext(BasicUser basicUser) {
                        loadedUsers.add(basicUser);
                        if (loadedUsers.size() % 10 == 0 && loadedUsers.size() > 1) {
                            subscription.request(10);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }
            );

        assertThat(loadedUsers.size()).isEqualTo(userCount);
    }


}
