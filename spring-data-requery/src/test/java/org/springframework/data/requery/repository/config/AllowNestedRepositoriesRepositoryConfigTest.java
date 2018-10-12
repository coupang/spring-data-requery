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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.repository.sample.ClassWithNestedRepository;
import org.springframework.data.requery.repository.sample.basic.BasicUserRepository;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AllowNestedRepositoriesRepositoryConfigTest
 *
 * @author debop
 * @since 18. 6. 14
 */
@ContextConfiguration
public class AllowNestedRepositoriesRepositoryConfigTest extends AbstractRepositoryConfigTest {

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = { BasicUserRepository.class, ClassWithNestedRepository.class },
                               considerNestedRepositories = true)
    static class TestConfiguration extends InfrastructureConfig {

    }

    @Autowired ClassWithNestedRepository.NestedUserRepository nestedUserRepository;


    @Test
    public void shouldFindNestedRepository() {
        assertThat(nestedUserRepository).isNotNull();
    }

}
