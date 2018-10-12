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

import io.requery.meta.EntityModel;
import io.requery.sql.EntityDataStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.basic.BasicUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * SimpleRequeryRepositoryTest
 *
 * @author debop
 * @since 18. 6. 12
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SimpleRequeryRepositoryTest {

    SimpleRequeryRepository<BasicUser, Long> repo;

    @Mock RequeryOperations operations;
    @Mock EntityDataStore<Object> entityDataStore;
    @Mock EntityModel entityModel;

    @Mock RequeryEntityInformation<BasicUser, Long> information;
    @Mock CrudMethodMetadata metadata;

    @Before
    public void setup() {

        when(operations.getDataStore()).thenReturn(entityDataStore);
        when(operations.getEntityModel()).thenReturn(entityModel);
        when(information.getJavaType()).thenReturn(BasicUser.class);

        repo = new SimpleRequeryRepository<>(information, operations);
        repo.setRepositoryMethodMetadata(metadata);
    }

    @Test
    public void retrieveObjectsForPageableOutOfRange() {
        Optional<BasicUser> user = repo.findById(1L);
        assertThat(user.isPresent()).isFalse();
        // repo.findAll(PageRequest.of(2, 10));
    }

}
