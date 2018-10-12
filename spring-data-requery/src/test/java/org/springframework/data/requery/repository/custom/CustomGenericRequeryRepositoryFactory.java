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
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.support.RequeryEntityInformation;
import org.springframework.data.requery.repository.support.RequeryRepositoryFactory;
import org.springframework.data.requery.repository.support.SimpleRequeryRepository;

import java.io.Serializable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * org.springframework.data.requery.repository.custom.CustomGenericRequeryRepositoryFactory
 *
 * @author debop
 * @since 18. 6. 9
 */
public class CustomGenericRequeryRepositoryFactory extends RequeryRepositoryFactory {

    public CustomGenericRequeryRepositoryFactory(RequeryOperations operations) {
        super(operations);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SimpleRequeryRepository<?, ?> getTargetRepository(RepositoryInformation information, RequeryOperations operations) {

        RequeryEntityInformation<Object, Serializable> entityMetadata = mock(RequeryEntityInformation.class);
        when(entityMetadata.getJavaType()).thenReturn((Class<Object>) information.getDomainType());
        return new CustomGenericRequeryRepository<>(entityMetadata, operations);

    }

    @Override
    protected @NotNull Class<?> getRepositoryBaseClass(@NotNull RepositoryMetadata metadata) {
        return CustomGenericRequeryRepository.class;
    }
}
