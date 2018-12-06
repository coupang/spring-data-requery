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

import io.requery.sql.EntityDataStore;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.core.RequeryOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static org.springframework.data.repository.query.QueryLookupStrategy.Key;

/**
 * Query lookup strategy to execute finders.
 *
 * @author debop
 * @since 18. 6. 9
 */
@Slf4j
@UtilityClass
public final class RequeryQueryLookupStrategy {

    @Nonnull
    public static QueryLookupStrategy create(@Nonnull final RequeryOperations operations,
                                             @Nullable final Key key,
                                             @Nonnull final QueryMethodEvaluationContextProvider evaluationContextProvider) {
        log.debug("Create Query Lookup Strategy with key={}", key);

        switch (key != null ? key : Key.CREATE_IF_NOT_FOUND) {
            case CREATE:
                log.trace("Create CreateQueryLookupStrategy instance.");
                return new CreateQueryLookupStrategy(operations);

            case USE_DECLARED_QUERY:
                log.trace("Create DeclaredQueryLookupStrategy instance.");
                return new DeclaredQueryLookupStrategy(operations, evaluationContextProvider);

            case CREATE_IF_NOT_FOUND:
                log.trace("Create CreateIfNotFoundQueryLookupStrategy instance.");
                return new CreateIfNotFoundQueryLookupStrategy(operations,
                                                               new CreateQueryLookupStrategy(operations),
                                                               new DeclaredQueryLookupStrategy(operations, evaluationContextProvider));
            default:
                throw new IllegalArgumentException("Unsupported query lookup strategy " + key);
        }
    }

    /**
     * Base class for {@link QueryLookupStrategy} implementations that need access to an {@link EntityDataStore}.
     */
    private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

        private final RequeryOperations operations;

        public AbstractQueryLookupStrategy(RequeryOperations operations) {
            this.operations = operations;
        }

        @Nonnull
        @Override
        public final RepositoryQuery resolveQuery(@Nonnull final Method method,
                                                  @Nonnull final RepositoryMetadata metadata,
                                                  @Nonnull final ProjectionFactory factory,
                                                  @Nonnull final NamedQueries namedQueries) {
            return resolveQuery(new RequeryQueryMethod(method, metadata, factory), operations, namedQueries);
        }

        protected abstract RepositoryQuery resolveQuery(@Nonnull final RequeryQueryMethod method,
                                                        @Nonnull final RequeryOperations operations,
                                                        @Nullable final NamedQueries namedQueries);
    }


    /**
     * {@link QueryLookupStrategy} to create a query from the queryMethod name.
     */
    private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

        public CreateQueryLookupStrategy(RequeryOperations operations) {
            super(operations);
        }

        @Override
        protected RepositoryQuery resolveQuery(@Nonnull final RequeryQueryMethod method,
                                               @Nonnull final RequeryOperations operations,
                                               @Nullable NamedQueries namedQueries) {
            log.debug("Create PartTreeRequeryQuery, queryMethod={}", method);
            return new PartTreeRequeryQuery(method, operations);
        }
    }

    /**
     * {@link QueryLookupStrategy} that tries to detect a declared query declared via {@link Query} annotation followed by
     * a Requery named query lookup.
     */
    private static class DeclaredQueryLookupStrategy extends AbstractQueryLookupStrategy {

        private final QueryMethodEvaluationContextProvider evaluationContextProvider;

        public DeclaredQueryLookupStrategy(RequeryOperations operations,
                                           QueryMethodEvaluationContextProvider evaluationContextProvider) {
            super(operations);
            this.evaluationContextProvider = evaluationContextProvider;
        }

        @Override
        protected RepositoryQuery resolveQuery(@Nonnull final RequeryQueryMethod method,
                                               @Nonnull final RequeryOperations operations,
                                               @Nullable final NamedQueries namedQueries) {
            // @Query annotation이 있다면 그 값으로 한다.
            if (method.isAnnotatedQuery()) {
                log.debug("Create DeclaredRequeryQuery for @Query annotated method. queryMethod={}", method.getName());
                return new DeclaredRequeryQuery(method, operations);
            }

            // TODO: spring-data-jpa 처럼 NamedQuery 를 만들어서 제공해야 한다.
            // NOTE: NamedQuery 가 하는 일이 Custom implemented method 에 대한 수행을 담당한다. ㅠ.ㅠ

            throw new IllegalStateException(
                String.format("Cannot find a annotated query for method %s!", method)
            );
        }
    }

    /**
     * {@link QueryLookupStrategy} to try to detect a declared query first (
     * {@link Query}, Requery named query). In case none is found we fall back on
     * query creation.
     */
    private static class CreateIfNotFoundQueryLookupStrategy extends AbstractQueryLookupStrategy {

        private final DeclaredQueryLookupStrategy lookupStrategy;
        private final CreateQueryLookupStrategy createStrategy;

        public CreateIfNotFoundQueryLookupStrategy(RequeryOperations operations,
                                                   CreateQueryLookupStrategy createStrategy,
                                                   DeclaredQueryLookupStrategy lookupStrategy) {
            super(operations);
            this.createStrategy = createStrategy;
            this.lookupStrategy = lookupStrategy;
        }

        @Override
        protected RepositoryQuery resolveQuery(@Nonnull final RequeryQueryMethod method,
                                               @Nonnull final RequeryOperations operations,
                                               @Nullable NamedQueries namedQueries) {
            try {
                log.debug("Resolve query by DeclaredQueryLookupStrategy...");
                return lookupStrategy.resolveQuery(method, operations, namedQueries);
            } catch (IllegalStateException se) {
                log.debug("Resolve query by CreateQueryLookupStrategy...");
                return createStrategy.resolveQuery(method, operations, namedQueries);
            }
        }
    }


}
