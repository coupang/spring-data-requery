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

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.query.RequeryQueryLookupStrategy;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Requery specific generic repository factory.
 *
 * @author debop
 * @since 18. 6. 4
 */
@Slf4j
public class RequeryRepositoryFactory extends RepositoryFactorySupport {

    @NotNull private final RequeryOperations operations;
    @NotNull private final CrudMethodMetadataPostProcessor crudMethodMetadataPostProcessor;

    public RequeryRepositoryFactory(@NotNull final RequeryOperations operations) {
        Assert.notNull(operations, "operations must not be null!");
        log.info("Create RequeryRepositoryFactory with operations={}", operations);

        this.operations = operations;
        this.crudMethodMetadataPostProcessor = new CrudMethodMetadataPostProcessor();
    }

    @Override
    public void setBeanClassLoader(@Nullable final ClassLoader classLoader) {
        super.setBeanClassLoader(classLoader);
        this.crudMethodMetadataPostProcessor.setBeanClassLoader(classLoader);
    }

    @NotNull
    @Override
    protected final SimpleRequeryRepository<?, ?> getTargetRepository(@NotNull final RepositoryInformation metadata) {

        SimpleRequeryRepository<?, ?> repository = getTargetRepository(metadata, operations);
        repository.setRepositoryMethodMetadata(crudMethodMetadataPostProcessor.getCrudMethodMetadata());

        Assert.isInstanceOf(SimpleRequeryRepository.class, repository);
        return repository;
    }

    @SuppressWarnings("unchecked")
    protected SimpleRequeryRepository<?, ?> getTargetRepository(@NotNull final RepositoryInformation information,
                                                                @NotNull final RequeryOperations operations) {
        log.debug("Get target repository. information={}", information);

        RequeryEntityInformation<?, ?> entityInformation = getEntityInformation(information.getDomainType());
        return getTargetRepositoryViaReflection(information, entityInformation, operations);
    }

    @NotNull
    @Override
    protected Class<?> getRepositoryBaseClass(@NotNull final RepositoryMetadata metadata) {
        return SimpleRequeryRepository.class;
    }

    @NotNull
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable final QueryLookupStrategy.Key key,
                                                                   @NotNull final EvaluationContextProvider evaluationContextProvider) {
        log.debug("Create QueryLookupStrategy by key={}", key);
        return Optional.of(RequeryQueryLookupStrategy.create(operations, key, evaluationContextProvider));
    }

    @Override
    @SuppressWarnings("unchecked")
    @NotNull
    public <T, ID> RequeryEntityInformation<T, ID> getEntityInformation(@NotNull final Class<T> domainClass) {
        return (RequeryEntityInformation<T, ID>) RequeryEntityInformationSupport.getEntityInformation(domainClass, operations);
    }
}
