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

package org.springframework.data.requery.kotlin.repository.query

import io.requery.query.NamedExpression
import io.requery.query.Result
import io.requery.query.element.QueryElement
import mu.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.parser.PartTree
import org.springframework.data.requery.kotlin.applyPageable
import org.springframework.data.requery.kotlin.applySort
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.mapping.RequeryMappingContext
import org.springframework.data.requery.kotlin.unwrap

/**
 * [PartTree] 정보를 바탕으로 Requery [QueryElement]를 빌드합니다.
 *
 * @author debop
 */
open class PartTreeRequeryQuery(queryMethod: RequeryQueryMethod,
                                operations: RequeryOperations) : AbstractRequeryQuery(queryMethod, operations) {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    private val tree: PartTree
    private val parameters: RequeryParameters

    private val countQueryPreparer: CountQueryPreparer
    private val queryPreparer: QueryPreparer
    private val context: RequeryMappingContext

    init {

        context = operations.mappingContext
        parameters = queryMethod.parameters

        log.debug { "Create PartTreeRequeryQuery. domainClass=$domainClass, parameters=$parameters" }

        try {
            this.tree = PartTree(queryMethod.name, domainClass)
            this.countQueryPreparer = CountQueryPreparer()
            this.queryPreparer = when {
                tree.isCountProjection -> countQueryPreparer
                else -> QueryPreparer()
            }
        } catch(e: Exception) {
            throw IllegalArgumentException("Fail to create query for method [$queryMethod], message=${e.message}", e)
        }
    }

    override fun doCreateQuery(values: Array<Any>): QueryElement<out Any> {
        return queryPreparer.createQuery(values).unwrap()
    }

    @Suppress("UNCHECKED_CAST")
    override fun doCreateCountQuery(values: Array<Any>): QueryElement<out Result<Int>> {
        return countQueryPreparer.createQuery(values).unwrap() as QueryElement<out Result<Int>>
    }

    override fun getExecution(): RequeryQueryExecution {
        return when {
            tree.isDelete -> DeleteExecution(operations)
            tree.isExistsProjection -> ExistsExecution()
            else -> super.getExecution()
        }
    }

    protected fun prepareQuery(accessor: RequeryParameterAccessor): QueryElement<out Any> {

        val query = buildWhereClause(operations.select(domainKlass).unwrap(), accessor)

        return when {

            accessor.parameters.hasPageableParameter() ->
                query.applyPageable(domainClass, accessor.pageable)

            accessor.parameters.hasSortParameter() ->
                query.applySort(domainClass, accessor.sort)

            else -> query
        }
    }

    protected fun buildWhereClause(baseQuery: QueryElement<out Any>, accessor: RequeryParameterAccessor): QueryElement<out Any> {

        val bindableParams = accessor.parameters.bindableParameters
        var query = baseQuery

        bindableParams.forEachIndexed { index, parameter ->
            val value = accessor.getBindableValue(index)

            if(parameter.isNamedParameter && parameter.name.isPresent) {
                log.trace { "Add named expression ... name=${parameter.name.get()}, value=$value" }
                val expr = NamedExpression.of(parameter.name.get(), value.javaClass)
                query = query.where(expr.eq(value)).unwrap()
            }
        }

        return query
    }

    @Suppress("LeakingThis")
    open inner class QueryPreparer {

        init {
            // HINT: check wrong method (parameter number matching, not exists property name ...)
            createCreator(null)?.let {
                it.createQuery()
                it.parameterExpressions
            }
        }

        fun createQuery(values: Array<Any>): QueryElement<out Any> {
            val accessor = RequeryParametersParameterAccessor(parameters, values)
            val creator = createCreator(accessor)!!

            var query = creator.createQuery(getDynamicSort(values))

            if(queryMethod.isPageQuery) {
                query = query.applyPageable(domainClass, accessor.pageable)
            }
            return restrictMaxResultsIfNecessary(query)
        }

        private fun restrictMaxResultsIfNecessary(baseQuery: QueryElement<out Any>): QueryElement<out Any> {

            var query = baseQuery

            if(tree.isLimiting) {
                if(query.limit != null) {
                    if(query.offset == null) {
                        query.offset(0)
                    }
                    /*
                     * In order to return the correct results, we have to adjust the first result offset to be returned if:
                     * - a Pageable parameter is present
                     * - AND the requested page number > 0
                     * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
                     */
                    if(query.limit > tree.maxResults ?: 0 && query.offset > 0) {
                        val offset = query.offset - (query.limit - tree.maxResults!!)
                        query = query.offset(offset).unwrap()
                    }
                }
                query = query.limit(tree.maxResults!!).unwrap()
            }

            if(tree.isExistsProjection) {
                query = query.limit(1).unwrap()
            }

            return query
        }

        protected open fun createCreator(accessor: RequeryParametersParameterAccessor?): RequeryQueryCreator? {

            val provider = when(accessor) {
                null -> ParameterMetadataProvider(parameters = parameters)
                else -> ParameterMetadataProvider(accessor = accessor)
            }

            val processor = queryMethod.resultProcessor

            val returnedType =
                if(accessor == null) processor.returnedType
                else processor.withDynamicProjection(accessor).returnedType

            return RequeryQueryCreator(operations, provider, returnedType, tree)

        }

        private fun getDynamicSort(values: Array<Any>): Sort = when {
            parameters.potentiallySortsDynamically() -> RequeryParametersParameterAccessor(parameters, values).sort
            else -> Sort.unsorted()
        }
    }

    /**
     * Special [QueryPreparer] to create count queries.
     */
    open inner class CountQueryPreparer : QueryPreparer() {

        override fun createCreator(accessor: RequeryParametersParameterAccessor?): RequeryQueryCreator? {

            val provider =
                if(accessor == null) ParameterMetadataProvider(parameters = parameters)
                else ParameterMetadataProvider(accessor = accessor)

            return RequeryCountQueryCreator(operations,
                                            provider,
                                            queryMethod.resultProcessor.returnedType,
                                            tree)
        }
    }
}