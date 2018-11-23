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

import io.requery.query.element.QueryElement
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.parser.PartTree
import org.springframework.data.requery.kotlin.CountRequery
import org.springframework.data.requery.kotlin.Requery
import org.springframework.data.requery.kotlin.applyPageable
import org.springframework.data.requery.kotlin.applySort
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.namedExpressionOf
import org.springframework.data.requery.kotlin.unwrap

/**
 * [PartTree] 정보를 바탕으로 Requery [QueryElement]를 빌드합니다.
 *
 * @author debop (Sunghyouk Bae)
 */
open class CoroutinePartTreeRequerQuery(queryMethod: RequeryQueryMethod,
                                        operations: CoroutineRequeryOperations)
    : AbstractCoroutineRequeryQuery(queryMethod, operations) {

    companion object : KLogging()

    private val tree: PartTree
    private val parameters: RequeryParameters

    private val countQueryPreparer: CountQueryPreparer
    private val queryPreparer: QueryPreparer


    init {
        logger.debug { "Create CoroutinePartTreeRequeryQuery. domainClass=$domainClassName" }
        try {
            this.parameters = queryMethod.parameters
            this.tree = PartTree(queryMethod.name, domainClass)
            this.countQueryPreparer = CountQueryPreparer()
            this.queryPreparer = if(tree.isCountProjection) countQueryPreparer else QueryPreparer()
        } catch(e: Exception) {
            throw IllegalArgumentException("Fail to create query for method [$queryMethod], message=${e.message}", e)
        }
    }

    override suspend fun doCreateQuery(values: Array<Any>): Requery {
        return queryPreparer.createQuery(values).unwrap()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun doCreateCountQuery(values: Array<Any>): CountRequery {
        return countQueryPreparer.createQuery(values).unwrap() as CountRequery
    }

    override fun getExecution(): AbstractCoroutineQueryExecution {
        return when {
            tree.isDelete -> CoroutineDeleteExecution(operations)
            tree.isExistsProjection -> CoroutineExistsExecution()
            else -> super.getExecution()
        }
    }

    protected open fun preapreQuery(accessor: RequeryParameterAccessor): Requery {
        logger.trace { "Prepare query ... domainClass=$domainClass" }

        val query = buildWhereClause(runBlocking { operations.select(domainKlass).unwrap() }, accessor)

        return when {
            accessor.parameters.hasPageableParameter() ->
                query.applyPageable(domainKlass, accessor.pageable)
            accessor.parameters.hasSortParameter() ->
                query.applySort(domainKlass, accessor.sort)
            else -> query
        }
    }

    protected open fun buildWhereClause(baseQuery: Requery, accessor: RequeryParameterAccessor): Requery {
        val bindableParams = accessor.parameters.bindableParameters
        var query = baseQuery

        // NOTE: 현재 Requery에서는 Named Parameter를 지원하지 않지만, 향후 사용할 예정이다.
        bindableParams.forEachIndexed { index, parameter ->
            val value = accessor.getBindableValue(index)
            if(parameter.isNamedParameter && parameter.name.isPresent) {
                logger.trace { "Add named expression ... name=${parameter.name}, value=$value" }
                val expr = namedExpressionOf(parameter.name.get(), value.javaClass)
                query = query.where(expr.eq(value)).unwrap()
            }
        }
        return query
    }

    /**
     * Query preparer
     */
    @Suppress("LeakingThis")
    open inner class QueryPreparer {

        init {
            logger.debug { "Check wrong method name or parameter name..." }
            createCreator(null)?.let { creator ->
                creator.createQuery()
                creator.parameterExpressions
            }
        }

        fun createQuery(values: Array<Any>): Requery {
            val accessor = RequeryParametersParameterAccessor(parameters, values)
            val creator = createCreator(accessor)
            check(creator != null) { "Creator must not be null." }

            var query = creator!!.createQuery(getDynamicSort(values))
            if(queryMethod.isPageQuery) {
                query = query.applyPageable(domainKlass, accessor.pageable)
            }

            return restrictMaxResultsIfNecessary(query)
        }

        private fun restrictMaxResultsIfNecessary(baseQuery: Requery): Requery {
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
            // exists projection 이라면 결과 레코드 수는 1개면 된다
            if(tree.isExistsProjection) {
                query = query.limit(1).unwrap()
            }
            return query
        }

        protected open fun createCreator(accessor: RequeryParametersParameterAccessor?): CoroutineRequeryQueryCreator? {
            logger.debug { "Create Query Creator ..." }

            val provider =
                if(accessor == null) ParameterMetadataProvider(parameters = parameters)
                else ParameterMetadataProvider(accessor = accessor)

            val returnedType =
                if(accessor == null) queryMethod.resultProcessor.returnedType
                else queryMethod.resultProcessor.withDynamicProjection(accessor).returnedType

            return CoroutineRequeryQueryCreator(operations, provider, returnedType, tree)
        }

        private fun getDynamicSort(values: Array<Any>): Sort {
            if(parameters.potentiallySortsDynamically()) {
                RequeryParametersParameterAccessor(parameters, values).sort
            }
            return Sort.unsorted()
        }
    }

    /**
     * Special [QueryPreparer] to create count queries.
     */
    open inner class CountQueryPreparer : QueryPreparer() {

        override fun createCreator(accessor: RequeryParametersParameterAccessor?): CoroutineRequeryQueryCreator? {
            logger.debug { "Create Count Query Creator ..." }
            val provider = accessor?.let { ParameterMetadataProvider(accessor = it) }
                           ?: ParameterMetadataProvider(parameters = parameters)

            return CoroutineRequeryCountQueryCreator(operations,
                                                     provider,
                                                     queryMethod.resultProcessor.returnedType,
                                                     tree)
        }
    }
}