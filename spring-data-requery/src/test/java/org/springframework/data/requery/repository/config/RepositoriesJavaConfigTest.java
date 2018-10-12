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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.requery.domain.sample.Product;
import org.springframework.data.requery.repository.sample.ProductRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RepositoriesJavaConfigTest
 *
 * @author debop
 * @since 18. 6. 14
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class RepositoriesJavaConfigTest {

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = { ProductRepository.class })
    static class TestConfig extends InfrastructureConfig {

        @Autowired ApplicationContext context;

        @Bean
        public Repositories repositories() {
            return new Repositories(context);
        }
    }

    @Autowired Repositories repositories;

    @Test
    public void getTargetRepositoryInstance() {
        assertThat(repositories.hasRepositoryFor(Product.class)).isTrue();
    }
}
