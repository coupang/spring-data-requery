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

package org.springframework.data.requery.repository.support;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.core.RequeryTemplate;
import org.springframework.data.requery.domain.RandomData;
import org.springframework.data.requery.domain.basic.BasicGroup;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { RequeryTestConfiguration.class })
public class RequeryRepositoryTest {

    @Autowired RequeryTemplate operations;

    SampleEntityRepository repository;
    GroupEntityRepository groupRepository;

    @Before
    public void setup() {
        assertThat(operations).isNotNull();

        repository = new RequeryRepositoryFactory(operations).getRepository(SampleEntityRepository.class);
        groupRepository = new RequeryRepositoryFactory(operations).getRepository(GroupEntityRepository.class);

        assertThat(repository).isNotNull();
        assertThat(groupRepository).isNotNull();

        repository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    public void testCrudOperationsForSimpleEntity() {

        BasicUser user = RandomData.randomUser();
        repository.save(user);

        assertThat(user.getId()).isNotNull();
        assertThat(repository.existsById(user.getId())).isTrue();
        assertThat(repository.count()).isEqualTo(1L);
        assertThat(repository.findById(user.getId())).isEqualTo(Optional.of(user));

        repository.deleteAll(Collections.singletonList(user));
        assertThat(repository.count()).isEqualTo(0L);
    }

    @Test
    public void executesCrudOperationsForEntity() {

        BasicGroup group = RandomData.randomBasicGroup();
        groupRepository.save(group);

        assertThat(group.getId()).isNotNull();
        assertThat(groupRepository.findById(group.getId())).isEqualTo(Optional.of(group));

        groupRepository.delete(group);
        assertThat(repository.count()).isEqualTo(0L);
    }

    @Test
    public void executeMultipleEntities() {

        int userCount = 10;
        Set<BasicUser> users = RandomData.randomUsers(userCount);

        log.debug("Add users... users size={}", users.size());

        repository.saveAll(users);
        assertThat(repository.count()).isEqualTo(userCount);

        int deleted = repository.deleteAllInBatch();
        assertThat(deleted).isEqualTo(userCount);
    }

    @Test
    public void executeInterfaceDefaultMethod() {

        BasicGroup group = RandomData.randomBasicGroup();
        groupRepository.save(group);

        assertThat(group.getId()).isNotNull();
        assertThat(groupRepository.findById(group.getId())).isEqualTo(Optional.of(group));

        List<BasicGroup> groups = groupRepository.findAllByName(group.getName());
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).isEqualTo(group);

        groupRepository.deleteAll(groups);
        assertThat(repository.count()).isEqualTo(0L);
    }

    private interface SampleEntityRepository extends RequeryRepository<BasicUser, Long> {

        @Query("select * from basic_user u where u.email = ?")
        @Transactional(readOnly = true)
        BasicUser findByAnnotatedQuery(String email);

    }

    private interface GroupEntityRepository extends RequeryRepository<BasicGroup, Integer> {

        default List<BasicGroup> findAllByName(String name) {
            return getOperations().select(BasicGroup.class)
                .where(BasicGroup.NAME.eq(name))
                .get()
                .toList();
        }
    }

}
