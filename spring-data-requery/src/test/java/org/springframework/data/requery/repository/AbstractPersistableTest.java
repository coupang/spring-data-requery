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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.domain.sample.CustomAbstractPersistable;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.sample.CustomAbstractPersistableRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@RunWith(SpringRunner.class)
@ContextConfiguration
public class AbstractPersistableTest {

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = { CustomAbstractPersistableRepository.class })
    static class TestConfiguration extends RequeryTestConfiguration {
    }

    @Autowired CustomAbstractPersistableRepository repository;

    @Test
    public void shouldBeAbleToSaveAndLoadCustomPersistableWithUuidId() {

        CustomAbstractPersistable entity = new CustomAbstractPersistable();
        CustomAbstractPersistable saved = repository.save(entity);
        CustomAbstractPersistable found = repository.findById(saved.getId()).get();

        assertThat(found).isEqualTo(saved);
    }

    @Test
    public void equalsWorksForProxiedEntities() {

        CustomAbstractPersistable entity = repository.save(new CustomAbstractPersistable());

        CustomAbstractPersistable proxy = repository.getOne(entity.getId());

        assertThat(proxy).isEqualTo(entity);
    }
}
