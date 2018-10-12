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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.requery.repository.sample.basic.BasicLocationRepository;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TypeFilterConfigTest
 *
 * @author debop
 * @since 18. 6. 14
 */
@Slf4j
@ContextConfiguration
public class TypeFilterConfigTest extends AbstractRepositoryConfigTest {

    @Configuration
    @EnableRequeryRepositories(
        basePackages = { "org.springframework.data.requery.**.repository.sample.basic" },
        excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { BasicLocationRepository.class }) })
    static class TestConfiguration extends InfrastructureConfig {

    }

    @Override
    public void testContextCreation() {
        assertThat(userRepository).isNotNull();
        assertThat(groupRepository).isNotNull();
        assertThat(locationRepository).isNull();
    }
}
