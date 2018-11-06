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

package org.springframework.boot.autoconfigure.data.requery;

import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.requery.configs.TestRequeryConfiguration;
import org.springframework.boot.autoconfigure.data.requery.domain.CityRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link RequeryRepositoriesAutoConfiguration}
 *
 * @author debop
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRequeryConfiguration.class })
@EnableRequeryRepositories(basePackageClasses = { CityRepository.class })
public class RequeryRepositoriesAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testDefaultRepositoryConfiguration() {

        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(beanName -> {
            log.debug("bean name={}", beanName);
            assertThat(beanName).isNotEmpty();
        });

        assertThat(applicationContext.getBean(EntityDataStore.class)).isNotNull();
        assertThat(applicationContext.getBean(PlatformTransactionManager.class)).isNotNull();
        assertThat(applicationContext.getBean(RequeryOperations.class)).isNotNull();

        assertThat(applicationContext.getBean(CityRepository.class)).isNotNull();
    }
}
