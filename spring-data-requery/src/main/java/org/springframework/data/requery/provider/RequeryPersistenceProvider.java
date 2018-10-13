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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Requery entity의 identifier 정보를 가져오는 클래스
 *
 * @author debop
 * @since 18. 6. 9
 */
@Slf4j
public class RequeryPersistenceProvider implements ProxyIdAccessor {

    @NotNull
    public static RequeryPersistenceProvider of(@NotNull final RequeryOperations operations) {
        Assert.notNull(operations, "operation must not be null!");
        return new RequeryPersistenceProvider(operations);
    }

    @Getter
    private final RequeryOperations operations;

    public RequeryPersistenceProvider(@NotNull final RequeryOperations operations) {
        Assert.notNull(operations, "operations must not be null!");
        this.operations = operations;
    }

    @Override
    public boolean shouldUseAccessorFor(@NotNull final Object entity) {
        return false;
    }

    @Nullable
    @Override
    public Object getIdentifierFrom(@NotNull final Object entity) {
        return null;
    }

    @Nullable
    public <T> Collection<T> potentiallyConvertEmptyCollection(@Nullable final Collection<T> collection) {
        return collection == null || collection.isEmpty() ? null : collection;
    }
}
