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

import io.requery.query.Result
import io.requery.query.element.QueryElement
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import mu.KLogging
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.requery.kotlin.NotSupportedException
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations

/**
 * CoroutineDeclaredRequeryQuery
 *
 * @author debop
 * @since 18. 10. 16
 */
class CoroutineDeclaredRequeryQuery(queryMethod: RequeryQueryMethod,
                                    operations: CoroutineRequeryOperations) : AbstractCoroutineRequeryQuery(queryMethod, operations) {

    companion object : KLogging()

    override suspend fun doCreateQuery(values: Array<Any>): QueryElement<out Any> {
        throw NotSupportedException("Unsupported operation in CoroutineDeclaredRequeryQuery.")
    }

    override suspend fun doCreateCountQuery(values: Array<Any>): QueryElement<out Result<Int>> {
        throw NotSupportedException("Unsupported operation in CoroutineDeclaredRequeryQuery.")
    }

    override fun execute(parameters: Array<Any>): Any? {
        val query = getNativeQuery()

        logger.debug { "Execute query. query=$query, return type=${queryMethod.returnType}" }

        // TODO: Refactoring이 필요하다.
        // TODO: Entity 나 Tuple 에 대해 ReturnedType에 맞게 casting 해야 한다.

        val accessor = RequeryParametersParameterAccessor(queryMethod.parameters, parameters)
        val pageable = accessor.pageable

        return when {
            pageable.isPaged -> {
                runBlocking {
                    val values = removePageable(accessor, parameters)

                    val countQuery = "select count(cnt_tbl.*) from ($query) as cnt_tbl"
                    val totals = withContext(Dispatchers.Unconfined) {
                        operations.raw(countQuery, *values).first().get<Long>(0)
                    }

                    val contentQuery = "$query offset ${pageable.offset} limit ${pageable.pageSize}"
                    val contents = withContext(Dispatchers.Unconfined) {
                        runNativeQuery(contentQuery, values)
                    }
                    contents.castResult(pageable, totals)
                }
            }
            else -> runBlocking {
                withContext(Dispatchers.Unconfined) {
                    runNativeQuery(query, parameters).castResult()
                }
            }
        }
    }

    private suspend fun runNativeQuery(query: String, parameters: Array<Any>): Result<*> {
        return if(queryMethod.isQueryForEntity) {
            operations.raw(queryMethod.entityKlass, query, *parameters)
        } else {
            operations.raw(query, *parameters)
        }
    }

    private fun Result<*>.castResult(pageable: Pageable = Pageable.unpaged(), totals: Long? = null): Any? {
        // TODO: List<Tuple> 인 경우 returned type 으로 변경해야 한다.
        return when {
            queryMethod.isCollectionQuery -> this.toList()
            queryMethod.isStreamQuery -> this.stream()
            queryMethod.isSliceQuery -> when {
                pageable.isPaged && totals != null -> {
                    val hasNext = totals > pageable.offset + pageable.pageSize
                    SliceImpl(this.toList(), pageable, hasNext)
                }
                else -> SliceImpl(this.toList())
            }
            queryMethod.isPageQuery -> when {
                pageable.isPaged && totals != null -> PageImpl(this.toList(), pageable, totals)
                else -> PageImpl(this.toList())
            }
            else -> RequeryResultConverter.convert(this.firstOrNull())
        }
    }

    private fun getNativeQuery(): String {
        val nativeQuery = queryMethod.annotatedQuery
        logger.trace { "Get native query. query=$nativeQuery" }

        if(nativeQuery.isNullOrBlank()) {
            error("No `@Query` query specified on ${queryMethod.name}")
        }

        return nativeQuery!!
    }

    private fun getReturnedType(parameters: Array<Any>): ReturnedType {
        val accessor = RequeryParametersParameterAccessor(queryMethod, parameters)
        val processor = queryMethod.resultProcessor

        val returnedType = processor.withDynamicProjection(accessor).returnedType
        logger.trace { "Return type is $returnedType" }

        return returnedType
    }

    // 인자 컬렉션의 Pageable 인스턴스의 index 를 찾아 parameters 에서 제거한다.
    private fun removePageable(accessor: RequeryParametersParameterAccessor, parameters: Array<Any>): Array<Any> {

        val pageableIndex = accessor.parameters.pageableIndex
        if(pageableIndex < 0) {
            return parameters
        }

        val values = Array<Any>(parameters.size - 1) {}
        var j = 0
        parameters.forEachIndexed { index, param ->
            if(index != pageableIndex) {
                values[j++] = param
            }
        }
        return values
    }

}