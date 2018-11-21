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

package org.springframework.data.requery.repository.support;

import io.requery.meta.EntityModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.query.DefaultRequeryEntityMetadata;
import org.springframework.data.requery.repository.query.RequeryEntityMetadata;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * {@link RequeryEntityInformation} 을 구현하여,
 *
 * @author debop
 * @since 18. 6. 7
 */
@Slf4j
public abstract class RequeryEntityInformationSupport<T, ID>
    extends AbstractEntityInformation<T, ID> implements RequeryEntityInformation<T, ID> {

    @Nonnull private final RequeryEntityMetadata<T> metadata;

    public RequeryEntityInformationSupport(@Nonnull final Class<T> domainClass) {
        super(domainClass);
        this.metadata = DefaultRequeryEntityMetadata.of(domainClass);
    }

    @Nonnull
    public String getEntityName() {
        return metadata.getEntityName();
    }

    @Nonnull
    public String getModelName() {
        return metadata.getModelName();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nonnull
    public static <T> RequeryEntityInformation<T, ?> getEntityInformation(@Nonnull final Class<T> domainClass,
                                                                          @Nonnull final RequeryOperations operations) {
        Assert.notNull(domainClass, "domainClass must not be null.");
        Assert.notNull(operations, "operations must not be null.");

        EntityModel entityModel = operations.getEntityModel();
        Assert.notNull(entityModel, "EntityModel must not be null!");
        log.debug("entityModel={}", entityModel);

        if (org.springframework.data.domain.Persistable.class.isAssignableFrom(domainClass)) {
            return new RequeryPersistableEntityInformation(domainClass, entityModel);
        } else {
            return new RequeryEntityModelEntityInformation<>(domainClass, entityModel);
        }
    }
}
