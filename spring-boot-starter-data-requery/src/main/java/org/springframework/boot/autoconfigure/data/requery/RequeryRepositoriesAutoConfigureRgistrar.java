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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.config.RequeryRepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * org.springframework.boot.autoconfigure.data.requery.RequeryRepositoriesAutoConfigureRgistrar
 *
 * @author debop
 */
public class RequeryRepositoriesAutoConfigureRgistrar
    extends AbstractRepositoryConfigurationSourceSupport {

    private static final Logger log = LoggerFactory.getLogger(RequeryRepositoriesAutoConfigureRgistrar.class);

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
