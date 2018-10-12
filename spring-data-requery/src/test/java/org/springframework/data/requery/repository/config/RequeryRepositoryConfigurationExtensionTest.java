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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.requery.repository.support.RequeryRepositoryFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequeryRepositoryConfigurationExtensionTest
 *
 * @author debop
 * @since 18. 6. 12
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class RequeryRepositoryConfigurationExtensionTest {

    @Mock RepositoryConfigurationSource configSource;

    public @Rule ExpectedException exception = ExpectedException.none();

    @Test
    public void requeryRepositoryClasses() {

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        RepositoryConfigurationExtension extension = new RequeryRepositoryConfigurationExtension();
        extension.registerBeansForRoot(factory, configSource);

        assertThat(extension.getModuleName()).isEqualTo("REQUERY");
        assertThat(extension.getRepositoryFactoryBeanClassName()).isEqualTo(RequeryRepositoryFactoryBean.class.getName());
    }
}
