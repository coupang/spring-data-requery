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

package org.springframework.data.requery.repository.custom;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.support.RequeryEntityInformation;
import org.springframework.data.requery.repository.support.SimpleRequeryRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * org.springframework.data.requery.repository.custom.CustomGenericRequeryRepository
 *
 * @author debop
 * @since 18. 6. 9
 */
@Transactional(readOnly = true)
public class CustomGenericRequeryRepository<T, ID>
    extends SimpleRequeryRepository<T, ID>
    implements CustomGenericRepository<T, ID> {

    public CustomGenericRequeryRepository(@NotNull RequeryEntityInformation<T, ID> entityInformation,
                                          @NotNull RequeryOperations operations) {
        super(entityInformation, operations);
    }

    @Override
    public T customMethod(ID id) {
        throw new UnsupportedOperationException("Forced exception for testing purposes");
    }
}
