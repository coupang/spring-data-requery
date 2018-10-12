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

package org.springframework.data.requery.repository.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.data.requery.repository.sample.basic.BasicGroupRepository;
import org.springframework.data.requery.repository.sample.basic.BasicLocationRepository;
import org.springframework.data.requery.repository.sample.basic.BasicUserRepository;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AbstractRepositoryConfigTest
 *
 * @author debop
 * @since 18. 6. 12
 */
@RunWith(SpringRunner.class)
public abstract class AbstractRepositoryConfigTest {

    @Autowired(required = false) BasicUserRepository userRepository;
    @Autowired(required = false) BasicGroupRepository groupRepository;
    @Autowired(required = false) BasicLocationRepository locationRepository;

    @Autowired RequeryMappingContext mappingContext;

    @Test
    public void testContextCreation() {
        assertThat(userRepository).isNotNull();
        assertThat(groupRepository).isNotNull();
        assertThat(locationRepository).isNotNull();
    }

    @Test
    public void repositoriesHaveExceptionTranslationApplied() {
        RequeryRepositoriesRegistrarIntegrationTests.assertExceptionTranslationActive(userRepository);
        RequeryRepositoriesRegistrarIntegrationTests.assertExceptionTranslationActive(groupRepository);
        RequeryRepositoriesRegistrarIntegrationTests.assertExceptionTranslationActive(locationRepository);
    }

    @Test
    public void exposesRequeryMappingContext() {
        assertThat(mappingContext).isNotNull();
    }
}
