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

package org.springframework.data.requery.provider;

import io.requery.meta.EntityModel;
import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.requery.utils.RequeryUtils;

/**
 * org.springframework.data.requery.provider.RequeryProxyIdAccessor
 *
 * @author debop
 * @since 18. 6. 7
 */
@Slf4j
public class RequeryProxyIdAccessor implements ProxyIdAccessor {

    private final EntityModel entityModel;

    public RequeryProxyIdAccessor(EntityDataStore entityDataStore) {
        this.entityModel = RequeryUtils.getEntityModel(entityDataStore);
    }

    @Override
    public boolean shouldUseAccessorFor(Object entity) {
        if (entity == null)
            return false;

        log.debug("Is requery entity?. entity={}", entity);

        return entityModel.containsTypeOf(entity.getClass());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public @Nullable Object getIdentifierFrom(Object entity) {
        if (entity == null)
            return null;

        log.debug("Get identifier value from entity. entity={}", entity);

        if (Persistable.class.isAssignableFrom(entity.getClass())) {
            return ((Persistable<?>) entity).getId();
        }

        return null;
    }
}
