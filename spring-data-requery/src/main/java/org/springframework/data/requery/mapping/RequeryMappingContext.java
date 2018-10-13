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

package org.springframework.data.requery.mapping;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * Requery Entity의 Mapping 정보를 제공합니다.
 *
 * @author debop
 * @since 18. 6. 8
 */
@Slf4j
public class RequeryMappingContext
    extends AbstractMappingContext<DefaultRequeryPersistentEntity<?>, RequeryPersistentProperty>
    implements ApplicationContextAware {

    private FieldNamingStrategy fieldNamingStrategy;

    @Nullable
    private ApplicationContext applicationContext;

    public void setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
        this.fieldNamingStrategy = fieldNamingStrategy;
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @NotNull
    @Override
    protected <T> DefaultRequeryPersistentEntity<?> createPersistentEntity(@NotNull TypeInformation<T> typeInformation) {
        final DefaultRequeryPersistentEntity<T> entity = new DefaultRequeryPersistentEntity<>(typeInformation);
        if (applicationContext != null) {
            entity.setApplicationContext(applicationContext);
        }
        log.debug("Create persistent entity. typeInformation={}", typeInformation);
        return entity;
    }

    @NotNull
    @Override
    protected RequeryPersistentProperty createPersistentProperty(@NotNull Property property,
                                                                 @NotNull DefaultRequeryPersistentEntity<?> owner,
                                                                 @NotNull SimpleTypeHolder simpleTypeHolder) {
        log.debug("Create property. property={}", property);
        return new DefaultRequeryPersistentProperty(property,
                                                    owner,
                                                    simpleTypeHolder,
                                                    fieldNamingStrategy);
    }
}
