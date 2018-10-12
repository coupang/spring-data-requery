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

package org.springframework.data.requery.repository;

import io.requery.query.Result;
import io.requery.query.element.QueryElement;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.sample.Child;
import org.springframework.data.requery.domain.sample.Parent;
import org.springframework.data.requery.domain.sample.Parent_Child;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.sample.ParentRepository;
import org.springframework.data.requery.repository.sample.UserRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

@Slf4j
@SuppressWarnings("unchecked")
@Transactional
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { RequeryTestConfiguration.class })
@EnableRequeryRepositories(basePackageClasses = { UserRepository.class })
public class ParentRepositoryTest {

    @Autowired ParentRepository repository;

    @Before
    public void setup() {
        repository.save(new Parent().addChild(new Child()));
        repository.save(new Parent().addChild(new Child()).addChild(new Child()));
        repository.save(new Parent().addChild(new Child()));
        repository.save(new Parent());
    }

    @Test
    public void testWithJoin() throws Exception {

        RequeryOperations ops = repository.getOperations();

        QueryElement<?> query = unwrap(ops
                                           .select(Parent.class)
                                           .distinct()
                                           .join(Parent_Child.class).on(Parent.ID.eq(Parent_Child.PARENT_ID)));

        Page<Parent> page = repository.findAll((QueryElement<? extends Result<Parent>>) query,
                                               PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id")));

        assertThat(page).isNotNull();
        for (Parent parent : page.getContent()) {
            log.debug("Parent={}", parent);
        }

        assertThat(page.getSize()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }
}
