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

import io.requery.async.CompletableEntityStore;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.RandomData;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Diego on 2018. 6. 10..
 */
public class CompletableFutureTest extends AbstractDomainTest {

    private CompletableEntityStore<Object> asyncEntityStore;

    @Before
    public void setup() {
        asyncEntityStore = new CompletableEntityStore<>(dataStore, ForkJoinPool.commonPool());

        requeryTemplate.deleteAll(BasicLocation.class);
        requeryTemplate.deleteAll(BasicGroup.class);
        requeryTemplate.deleteAll(BasicUser.class);
    }

    @Test
    public void async_insert() {
        BasicUser user = RandomData.randomUser();
        asyncEntityStore.insert(user).thenAccept(u -> {
            assertThat(u.getId()).isNotNull();

            BasicUser loaded = asyncEntityStore.select(BasicUser.class).where(BasicUser.ID.eq(user.id)).get().first();
            assertThat(loaded).isEqualTo(user);
        }).join();
    }

    @Test
    public void async_insert_and_count() throws Exception {
        BasicUser user = RandomData.randomUser();

        asyncEntityStore.insert(user)
            .thenAccept(savedUser -> assertThat(savedUser.id).isNotNull())
            .thenCompose(it -> asyncEntityStore.count(BasicUser.class)
                .get()
                .toCompletableFuture())
            .get();
    }

    @Test
    public void async_insert_one_to_many() throws Exception {
        BasicUser user = RandomData.randomUser();

        asyncEntityStore
            .insert(user)
            .thenApply(savedUser -> {
                BasicGroup group = new BasicGroup();
                group.setName("group");
                savedUser.getGroups().add(group);
                return group;
            })
            .thenCompose(group -> asyncEntityStore.insert(group))
            .get();

        assertThat(user.groups).hasSize(1);
    }

    @Test
    public void async_query_update() throws Exception {
        BasicUser user = RandomData.randomUser();
        user.setAge(100);

        asyncEntityStore
            .insert(user)
            .thenCompose(savedUser -> asyncEntityStore.update(BasicUser.class)
                .set(BasicUser.ABOUT, "nothing")
                .set(BasicUser.AGE, 50)
                .where(BasicUser.AGE.eq(user.age))
                .get()
                .toCompletableFuture(Executors.newSingleThreadExecutor()))
            .thenApplyAsync(rowCount -> assertThat(rowCount).isEqualTo(1))
            .get();
    }

    @Test
    public void async_to_blocking() {
        BasicUser user = RandomData.randomUser();
        asyncEntityStore.toBlocking().insert(user);
        assertThat(user.id).isNotNull();
    }

    @Test
    public void query_stream() {
        Set<BasicUser> users = RandomData.randomUsers(100);

        asyncEntityStore.insert(users).join();

        Stream<BasicUser> loadedUsers = asyncEntityStore.select(BasicUser.class)
            .orderBy(BasicUser.NAME.asc())
            .limit(200)
            .get()
            .stream();

        assertThat(loadedUsers.count()).isEqualTo(100L);
    }
}
