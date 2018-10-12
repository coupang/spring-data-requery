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

package org.springframework.data.requery.repository.query;

import io.requery.query.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.RandomData;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.data.requery.repository.support.RequeryRepositoryFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * org.springframework.data.requery.repository.query.DeclaredRequeryQueryTest
 *
 * @author debop
 * @since 18. 6. 16
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { RequeryTestConfiguration.class })
public class DeclaredRequeryQueryTest {

    @Inject RequeryOperations operations;

    SampleQueryRepository repository;

    @Before
    public void setup() {
        assertThat(operations).isNotNull();
        repository = new RequeryRepositoryFactory(operations).getRepository(SampleQueryRepository.class);

        assertThat(repository).isNotNull();
        repository.deleteAll();
    }

    @Test
    public void singleResultRawQuery() {

        BasicUser user = RandomData.randomUser();
        repository.save(user);

        BasicUser loaded = repository.findByAnnotatedQuery(user.getEmail());

        log.debug("loaded user={}", loaded);
        assertThat(loaded).isNotNull();
    }

    @Test
    public void collectionResultRawQuery() {
        Set<BasicUser> users = RandomData.randomUsers(100);
        repository.saveAll(users);

        assertThat(repository.count()).isGreaterThan(0);

        List<BasicUser> results = repository.findAllByEmailMatches("debop%");
        assertThat(results.size()).isGreaterThan(0);
    }

    @Test
    public void queryWithLimits() {
        Set<BasicUser> users = RandomData.randomUsers(10);
        repository.saveAll(users);

        assertThat(repository.count()).isGreaterThan(0);

        List<BasicUser> results = repository.findWithLimits(5);
        assertThat(results).hasSize(5);
    }

    @Test
    public void multipleParameterQuery() {
        Set<BasicUser> users = RandomData.randomUsers(4);
        repository.saveAll(users);

        BasicUser user = RandomData.randomUser();
        user.setName("배성혁");
        repository.save(user);

        BasicUser loaded = repository.findAllBy(user.getName(), user.getEmail(), 1);

        assertThat(loaded).isEqualTo(user);
    }

    @Test
    public void queryTuples() {
        Set<BasicUser> users = RandomData.randomUsers(4);
        repository.saveAll(users);

        BasicUser user = RandomData.randomUser();
        user.setEmail("debop@example.com");
        repository.save(user);

        List<Tuple> loaded = repository.findAllIds(user.getEmail());
        assertThat(loaded).hasSize(1);
        assertThat(loaded.get(0).<Long>get("id")).isEqualTo(user.getId());
        assertThat(loaded.get(0).<String>get("name")).isEqualTo(user.getName());
    }


    @Test
    public void queryByLocalData() {

        BasicUser user = RandomData.randomUser();
        repository.save(user);

        List<BasicUser> loaded = repository.findByBirthday(user.getBirthday());
        assertThat(loaded).hasSize(1);
        assertThat(loaded.get(0)).isEqualTo(user);

        List<BasicUser> notexists = repository.findByBirthday(LocalDate.ofEpochDay(0));
        assertThat(notexists).isEmpty();
    }

    interface SampleQueryRepository extends RequeryRepository<BasicUser, Long> {

        @Query("select * from basic_user u where u.email = ?")
        BasicUser findByAnnotatedQuery(String email);

        @Query("select * from basic_user u where u.email like ?")
        List<BasicUser> findAllByEmailMatches(String email);

        @Query("select * from basic_user u limit ?")
        List<BasicUser> findWithLimits(int limit);

        @Query("select * from basic_user u where u.name=? and u.email=? limit ?")
        BasicUser findAllBy(String name, String email, int limit);

        @Query("select u.id, u.name from basic_user u where u.email=?")
        List<Tuple> findAllIds(String email);

        @Query("select * from basic_user u where u.birthday = ?")
        List<BasicUser> findByBirthday(LocalDate birthday);
    }

}
