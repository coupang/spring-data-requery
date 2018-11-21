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

package org.springframework.data.requery.repository.query;

import io.requery.query.Scalar;
import io.requery.query.Tuple;
import io.requery.query.element.QueryElement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.CollectionExecution;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.PagedExecution;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.SingleEntityExecution;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.SlicedExecution;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.StreamExecution;
import org.springframework.data.requery.utils.RequeryMetamodel;
import org.springframework.data.requery.utils.RequeryUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;


/**
 * Abstract base class to implement {@link RepositoryQuery}s.
 *
 * @author debop
 * @since 18. 6. 7
 */
@Getter
@Slf4j
public abstract class AbstractRequeryQuery implements RepositoryQuery {

    protected final RequeryQueryMethod queryMethod;
    protected final RequeryOperations operations;
    protected final RequeryMetamodel metamodel;
    protected final Class<?> domainClass;

    public AbstractRequeryQuery(@Nonnull final RequeryQueryMethod method,
                                @Nonnull final RequeryOperations operations) {
        Assert.notNull(method, "queryMethod must not be null");
        Assert.notNull(operations, "operations must not be null");

        this.queryMethod = method;
        this.operations = operations;
        this.metamodel = new RequeryMetamodel(RequeryUtils.getEntityModel(operations.getDataStore()));
        this.domainClass = method.getEntityInformation().getJavaType();
    }

    @SuppressWarnings("NullableProblems")
    @Nullable
    public Object execute(@Nonnull final Object[] parameters) {
        return doExecute(getExecution(), parameters);
    }

    @Nullable
    private Object doExecute(@Nonnull RequeryQueryExecution execution, Object[] values) {

        Object result = execution.execute(this, values);
        // ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), values);

        log.debug("doExecute ... result={}", result);
        return result;
    }

    @Nonnull
    protected RequeryQueryExecution getExecution() {

        RequeryQueryExecution execution;

        // NOTE: 검사하는 순서가 중요합니다.
        if (queryMethod.isStreamQuery()) {
            execution = new StreamExecution(queryMethod.getParameters());
        } else if (queryMethod.isCollectionQuery()) {
            execution = new CollectionExecution();
        } else if (queryMethod.isSliceQuery()) {
            execution = new SlicedExecution(queryMethod.getParameters());
        } else if (queryMethod.isPageQuery()) {
            execution = new PagedExecution(queryMethod.getParameters());
        } else {
            execution = new SingleEntityExecution();
        }
        log.debug("Create RequeryQueryExecution. execution={}, queryMethod={}", execution.getClass().getSimpleName(), queryMethod);
        return execution;
    }

    @Nonnull
    protected QueryElement<?> createQueryElement(@Nonnull final Object[] values) {
        log.debug("Create QueryElement with domainClass={}, values={}", domainClass.getName(), values);
        return doCreateQuery(values);
    }

    @Nonnull
    protected QueryElement<? extends Scalar<Integer>> createCountQueryElement(@Nonnull final Object[] values) {
        return doCreateCountQuery(values);
    }

    @Nonnull
    protected Optional<Class<?>> getTypeToRead(@Nonnull final ReturnedType returnedType) {
        return returnedType.isProjecting() && !getMetamodel().isRequeryManaged(returnedType.getReturnedType())
               ? Optional.of(Tuple.class)
               : Optional.empty();
    }

    @Nonnull
    protected abstract QueryElement<?> doCreateQuery(@Nonnull final Object[] values);

    @Nonnull
    protected abstract QueryElement<? extends Scalar<Integer>> doCreateCountQuery(@Nonnull final Object[] values);


}

