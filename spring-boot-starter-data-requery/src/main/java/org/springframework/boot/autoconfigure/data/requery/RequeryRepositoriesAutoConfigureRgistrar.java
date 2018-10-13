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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.config.RequeryRepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Requery 를 위한 {@link ImportBeanDefinitionRegistrar} used to auto-configure Spring Data Repositories.
 *
 * @author debop
 */
@Slf4j
public class RequeryRepositoriesAutoConfigureRgistrar
    extends AbstractRepositoryConfigurationSourceSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableRequeryRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableRequeryRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        log.debug("Create RepositoryConfigurationExtension");
        return new RequeryRepositoryConfigurationExtension();
    }

    @EnableRequeryRepositories
    private static class EnableRequeryRepositoriesConfiguration {
        // Nothing to do 
    }
}
