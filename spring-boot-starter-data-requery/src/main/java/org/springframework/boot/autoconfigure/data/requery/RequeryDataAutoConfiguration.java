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
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.core.RequeryTemplate;
import org.springframework.data.requery.mapping.RequeryMappingContext;

import javax.sql.DataSource;

/**
 * org.springframework.boot.autoconfigure.data.requery.RequeryAutoConfiguration
 *
 * @author debop
 */
@Slf4j
@Configuration
@ConditionalOnBean({ DataSource.class, EntityDataStore.class })
@AutoConfigureAfter(RequeryAutoConfiguration.class)
public class RequeryDataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EntityDataStore.class)
    public RequeryOperations requeryOperations(@NotNull final EntityDataStore<Object> entityDataStore,
                                               @NotNull final RequeryMappingContext mappingContext) {

        log.info("Create RequeryOperations ...");
        try {
            return new RequeryTemplate(entityDataStore, mappingContext);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RequeryMappingContext mappingContext(@NotNull final ApplicationContext applicationContext) {
        RequeryMappingContext mappingContext = new RequeryMappingContext();
        mappingContext.setApplicationContext(applicationContext);
        return mappingContext;
    }


}
