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
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;

/**
 * Extension of {@link RequeryEntityModelEntityInformation} that consideres methods of {@link Persistable} to lookup the id.
 *
 * @author debop
 * @since 18. 6. 7
 */
@Slf4j
public class RequeryPersistableEntityInformation<T extends Persistable<ID>, ID>
    extends RequeryEntityModelEntityInformation<T, ID> {

    public RequeryPersistableEntityInformation(@NotNull Class<T> domainClass,
                                               @NotNull EntityModel entityModel) {
        super(domainClass, entityModel);
    }

    @Override
    public boolean isNew(@NotNull T entity) {
        Assert.notNull(entity, "entity must not be nulll");
        log.trace("is new entity. entity={}", entity);
        return entity.isNew();
    }

    @Override
    public ID getId(@NotNull T entity) {
        Assert.notNull(entity, "entity must not be nulll");
        log.trace("get id of entity. entity={}", entity);
        return entity.getId();
    }
}
