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

package org.springframework.data.requery.mapping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.basic.AbstractBasicUser;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.sample.basic.BasicUserRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequeryMappingContextIntegrationTest
 *
 * @author debop
 * @since 18. 6. 12
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class RequeryMappingContextIntegrationTest {

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = { BasicUserRepository.class },
                               includeFilters = @ComponentScan.Filter(value = { BasicUserRepository.class },
                                                                      type = FilterType.ASSIGNABLE_TYPE))
    static class Config extends RequeryTestConfiguration {

    }

    RequeryMappingContext context;

    @Inject BasicUserRepository users;

    @Inject RequeryOperations operations;

    @Before
    public void setup() {
        context = new RequeryMappingContext();
    }

    @Test
    public void setupMappingContextCorrectly() {

        RequeryPersistentEntity<?> entity = context.getRequiredPersistentEntity(AbstractBasicUser.class);
        assertThat(entity).isNotNull();
    }
}
