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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.data.requery.repository.config.RequeryRepositoryConfigurationExtension;
import org.springframework.data.requery.repository.support.RequeryRepositoryFactoryBean;

/**
 * Requery Repository 를 위한 auto configuration
 *
 * @author debop
 */
@Slf4j
@Configuration
@ConditionalOnBean({ RequeryOperations.class })
@ConditionalOnClass({ RequeryRepository.class })
@ConditionalOnMissingBean({ RequeryRepositoryFactoryBean.class, RequeryRepositoryConfigurationExtension.class })
@ConditionalOnProperty(prefix = "spring.data.requery.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(RequeryRepositoriesAutoConfigureRgistrar.class)
@AutoConfigureAfter(RequeryDataAutoConfiguration.class)
public class RequeryRepositoriesAutoConfiguration {
}
