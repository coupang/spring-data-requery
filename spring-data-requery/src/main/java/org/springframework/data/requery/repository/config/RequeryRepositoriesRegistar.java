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
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * {@link ImportBeanDefinitionRegistrar} to enable {@link EnableRequeryRepositories} annotation.
 *
 * @author debop
 * @since 18. 6. 6
 */
@Slf4j
public class RequeryRepositoriesRegistar extends RepositoryBeanDefinitionRegistrarSupport {

    @NotNull
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableRequeryRepositories.class;
    }

    @NotNull
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new RequeryRepositoryConfigurationExtension();
    }

}
