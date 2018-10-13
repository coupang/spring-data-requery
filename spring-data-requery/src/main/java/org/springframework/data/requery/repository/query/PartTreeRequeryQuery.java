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

import io.requery.query.NamedExpression;
import io.requery.query.Scalar;
import io.requery.query.element.QueryElement;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.DeleteExecution;
import org.springframework.data.requery.repository.query.RequeryQueryExecution.ExistsExecution;
import org.springframework.data.requery.utils.RequeryUtils;

import java.util.Optional;

import static org.springframework.data.requery.utils.RequeryUtils.applyPageable;
import static org.springframework.data.requery.utils.RequeryUtils.applySort;
import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * {@link PartTree} 정보를 바탕으로 Requery {@link QueryElement} 를 빌드합니다.
 *
 * @author debop
 * @since 18. 6. 9
 */
@Slf4j
public class PartTreeRequeryQuery extends AbstractRequeryQuery {

    private final PartTree tree;
    private final RequeryParameters parameters;

    private final QueryPreparer queryPreparer;
    private final CountQueryPreparer countQueryPreparer;
    private final RequeryOperations operations;
    private final RequeryMappingContext context;


    public PartTreeRequeryQuery(@NotNull final RequeryQueryMethod method,
                                @NotNull final RequeryOperations operations) {
        super(method, operations);

        this.operations = operations;
        this.context = operations.getMappingContext();
        this.parameters = method.getParameters();

        log.debug("Create PartTreeRequeryQuery. domainClass={}, parameters={}", getDomainClass(), parameters);

        try {
            this.tree = new PartTree(method.getName(), domainClass);
            this.countQueryPreparer = new CountQueryPreparer();
            this.queryPreparer = tree.isCountProjection() ? countQueryPreparer : new QueryPreparer();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fail to create query for method [" + method + "] message=" + e.getMessage(), e);
        }
    }

    @NotNull
    @Override
    protected QueryElement<?> doCreateQuery(@NotNull final Object[] values) {
        return queryPreparer.createQuery(values);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    protected QueryElement<? extends Scalar<Integer>> doCreateCountQuery(@NotNull final Object[] values) {
        return (QueryElement<? extends Scalar<Integer>>) countQueryPreparer.createQuery(values);
    }

    @NotNull
    @Override
    protected RequeryQueryExecution getExecution() {
        if (tree.isDelete()) {
            log.debug("Create DeleteExecution. queryMethod={}", queryMethod);
            return new DeleteExecution(operations);
        } else if (tree.isExistsProjection()) {
            log.debug("Create ExistsExecution. queryMethod={}", queryMethod);
            return new ExistsExecution();
        }
        return super.getExecution();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected QueryElement<?> prepareQuery(@NotNull final RequeryParameterAccessor accessor) {
        QueryElement<?> query = unwrap(getOperations().select(getDomainClass()));

        query = buildWhereClause(query, accessor);

        if (accessor.getParameters().hasPageableParameter()) {
            query = applyPageable(getDomainClass(), query, accessor.getPageable());
        }
        if (accessor.getParameters().hasSortParameter()) {
            query = applySort(getDomainClass(), query, accessor.getSort());
        }

        return query;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected QueryElement<?> buildWhereClause(@NotNull final QueryElement<?> baseQuery,
                                               @NotNull RequeryParameterAccessor accessor) {

        final RequeryParameters bindableParams = accessor.getParameters().getBindableParameters();
        final int bindableParamsSize = bindableParams.getNumberOfParameters();

        QueryElement<?>[] queries = new QueryElement[1];
        queries[0] = baseQuery;

        for (int i = 0; i < bindableParamsSize; i++) {
            final RequeryParameters.RequeryParameter param = bindableParams.getParameter(i);
            final Object value = accessor.getBindableValue(i);

            if (param.isNamedParameter() && param.getName().isPresent()) {
                NamedExpression expr = NamedExpression.of(param.getName().get(), value.getClass());
                queries[0] = unwrap(queries[0].where(expr.eq(value)));
            }
        }
        return queries[0];
    }


    /**
     * Query preparer to create {@link QueryElement} instances and potentially cache them.
     */
    private class QueryPreparer {

        QueryPreparer() {

            // HINT: check wrong method (parameter number matching, not exists property name ...)
            RequeryQueryCreator creator = createCreator(null);
            if (creator != null) {
                creator.createQuery();
                creator.getParameterExpressions();
            }
        }

        @NotNull
        public QueryElement<?> createQuery(@NotNull final Object[] values) {

            RequeryParametersParameterAccessor accessor = new RequeryParametersParameterAccessor(parameters, values);
            RequeryQueryCreator creator = createCreator(accessor);

            if (creator != null) {
                QueryElement<?> query = creator.createQuery(getDynamicSort(values));

                if (getQueryMethod().isPageQuery()) {
                    query = RequeryUtils.applyPageable(getDomainClass(), query, accessor.getPageable());
                }
                return restrictMaxResultsIfNecessary(query);
            }
            throw new IllegalStateException("Fail to create RequeryQueryCreator.");
        }

        @SuppressWarnings("ConstantConditions")
        @NotNull
        private QueryElement<?> restrictMaxResultsIfNecessary(@NotNull final QueryElement<?> baseQuery) {

            QueryElement<?> query = baseQuery;

            if (tree.isLimiting()) {
                if (query.getLimit() != null) {
                    if (query.getOffset() == null) {
                        query.offset(0);
                    }
                    /*
                     * In order to return the correct results, we have to adjust the first result offset to be returned if:
                     * - a Pageable parameter is present
                     * - AND the requested page number > 0
                     * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
                     */
                    if (query.getLimit() > Optional.ofNullable(tree.getMaxResults()).orElse(0) && query.getOffset() > 0) {
                        int offset = query.getOffset() - (query.getLimit() - tree.getMaxResults());
                        query = unwrap(query.offset(offset));
                    }
                }
                query = unwrap(query.limit(tree.getMaxResults()));
            }

            if (tree.isExistsProjection()) {
                query = unwrap(query.limit(1));
            }

            return query;
        }

        @Nullable
        protected RequeryQueryCreator createCreator(@Nullable final RequeryParametersParameterAccessor accessor) {

            ParameterMetadataProvider provider = (accessor != null)
                                                 ? new ParameterMetadataProvider(accessor)
                                                 : new ParameterMetadataProvider(parameters);

            ResultProcessor processor = getQueryMethod().getResultProcessor();
            ReturnedType returnedType = (accessor != null)
                                        ? processor.withDynamicProjection(accessor).getReturnedType()
                                        : processor.getReturnedType();

            return new RequeryQueryCreator(operations, provider, returnedType, tree);
        }

        private Sort getDynamicSort(Object[] values) {
            return parameters.potentiallySortsDynamically()
                   ? new RequeryParametersParameterAccessor(parameters, values).getSort()
                   : Sort.unsorted();
        }

    }

    /**
     * Special {@link QueryPreparer} to create count queries.
     */
    private class CountQueryPreparer extends QueryPreparer {

        @Override
        protected RequeryCountQueryCreator createCreator(@Nullable final RequeryParametersParameterAccessor accessor) {
            ParameterMetadataProvider provider = (accessor != null)
                                                 ? new ParameterMetadataProvider(accessor)
                                                 : new ParameterMetadataProvider(parameters);

            return new RequeryCountQueryCreator(operations,
                                                provider,
                                                getQueryMethod().getResultProcessor().getReturnedType(),
                                                tree);
        }
    }
}
