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

import io.requery.PersistenceException
import io.requery.query.Result
import io.requery.query.element.QueryElement
import io.requery.query.function.Count
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.ConfigurableConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.data.repository.query.ParametersParameterAccessor
import org.springframework.data.requery.kotlin.applyPageable
import org.springframework.data.requery.kotlin.applyWhereConditions
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.getAsResult
import org.springframework.data.requery.kotlin.getAsScalarInt
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.util.ClassUtils
import java.util.stream.Stream

/**
 * Coroutine 용 [RequeryQueryExecution]
 *
 * @author debop (Sunghyouk Bae)
 */
abstract class AbstractCoroutineQueryExecution {

    companion object : KLogging() {

        @JvmStatic
        private val CONVERSION_SERVICE: ConversionService by lazy {
            val conversionService = DefaultConversionService()

            // Blob to Byte array 로 하는 것은 BlobByteArrayConverter 를 사용하면 된다.
            // conversionService.addConverter(JpaResultConverters.BlobToByteArrayConverter.INSTANCE);

            conversionService.removeConvertible(Collection::class.java, Any::class.java)
            potentiallyRemoveOptionalConverter(conversionService)

            conversionService
        }

        @JvmStatic
        fun potentiallyRemoveOptionalConverter(conversoinService: ConfigurableConversionService) {

            val classLoader = RequeryQueryExecution::class.java.classLoader

            if(ClassUtils.isPresent("java.util.Optional", classLoader)) {
                try {
                    val optionalType = ClassUtils.forName("java.util.Optional", classLoader)
                    conversoinService.removeConvertible(Any::class.java, optionalType)
                } catch(e: ClassNotFoundException) {
                    // Nothing to do.
                } catch(e: LinkageError) {
                    // Nothing to do.
                }
            }
        }
    }

    suspend fun execute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Any? {
        return try {
            doExecute(query, values)
        } catch(e: PersistenceException) {
            logger.error(e) { "Fail to execute query. return null. query=$query" }
            null
        }
    }

    protected abstract suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Any?

    /**
     * method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
     */
    protected fun adjustPage(queryElement: QueryElement<out Any>,
                             domainClass: Class<out Any>,
                             pageable: Pageable): QueryElement<out Any> {
        // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
        val needPageable = queryElement.limit == null && queryElement.offset == null

        return when {
            needPageable -> queryElement.applyPageable(domainClass, pageable).unwrap()
            else -> queryElement
        }
    }
}

/**
 * Coroutine 을 이용하여 Requery의 결과를 Collection으로 받을 수 있도록 합니다.
 *
 * @author debop
 * @since 18. 10. 16
 */
internal class CoroutineCollectionExecution : AbstractCoroutineQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): List<Any> {
        val result = query.createQueryElement(values).get() as Result<*>
        return result.toList()
    }
}

/**
 * 쿼리를 실행하여, 결과를 [Slice] 수형으로 반환합니다.
 */
internal class CoroutineSlicedExecution(val parameters: RequeryParameters) : AbstractCoroutineQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Slice<*> {
        logger.debug { "execute requery query, then return sliced list" }
        val accessor = ParametersParameterAccessor(parameters, values)
        val pageable = accessor.pageable

        var queryElement = query.createQueryElement(values)

        return if(pageable.isPaged) {
            // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
            if(queryElement.limit == null) {
                queryElement = queryElement.applyPageable(query.domainKlass, pageable)
            }

            val minLimit = minOf(queryElement.limit, pageable.pageSize)
            queryElement = queryElement.offset(pageable.pageNumber * minLimit).unwrap()

            val pageSize = queryElement.limit
            queryElement = queryElement.limit(pageSize + 1).unwrap()

            logger.trace { "Make sliced parameters. Offset=${queryElement.offset}, limit=${queryElement.limit}, pageSize=$pageSize" }

            val contents = queryElement.getAsResult().toList()
            val hasNext = contents.size > pageSize
            val slicedContents = if(hasNext) contents.subList(0, pageSize) else contents

            SliceImpl(slicedContents, pageable, hasNext)
        } else {
            val result = queryElement.getAsResult()
            SliceImpl(result.toList())
        }
    }
}

/**
 * 쿼리를 실행하여, 결과를 [Page] 수형으로 반환합니다.
 */
internal class CoroutinePagedExecution(val parameters: RequeryParameters) : AbstractCoroutineQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Page<*> {
        logger.debug { "Run paged execution... query=$query, values=$values" }

        val accessor = ParametersParameterAccessor(parameters, values)
        val pageable = accessor.pageable

        return if(pageable.isPaged) {
            // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
            var queryElement = query.createQueryElement(values)
            queryElement = adjustPage(queryElement, query.domainClass, pageable)
            logger.trace { "offset=${queryElement.offset}, limit=${queryElement.limit}, pageable=$pageable" }

            val result = withContext(Dispatchers.Default) { queryElement.getAsResult().toList() }
            val totals = withContext(Dispatchers.Default) { doExecuteTotals(query, values) }

            PageImpl(result, pageable, totals)
        } else {
            val queryElement = query.createQueryElement(values)
            PageImpl(queryElement.getAsResult().toList())
        }
    }

    private suspend fun doExecuteTotals(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Long {
        val baseQuery = query.createQueryElement(values).unwrap()
        val selection = query.operations.select(Count.count(query.domainClass))

        selection.unwrap().whereElements.addAll(baseQuery.whereElements)

        val result = RequeryResultConverter.convert(selection.get().firstOrNull(), 0L) as Int
        return result.toLong()
    }
}

/**
 * 쿼리를 실행하여, Single Entity를 반환합니다.
 */
internal class CoroutineSingleEntityExecution : AbstractCoroutineQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Any? {
        logger.debug { "Get single entity... query=$query, values=$values" }

        val result = query.createQueryElement(values).getAsResult().firstOrNull()
        return RequeryResultConverter.convert(result)
    }
}

/**
 * 쿼리를 실행하여, 엔티티의 목록을 Stream으로 반환합니다.
 */
internal class CoroutineStreamExecution(val parameters: RequeryParameters) : AbstractCoroutineQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Stream<out Any> {
        logger.debug { "Get stream of entities. query=$query, values=$values" }

        val accessor = ParametersParameterAccessor(parameters, values)
        val pageable = accessor.pageable

        var queryElem = query.createQueryElement(values)

        if(pageable.isPaged) {
            // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
            queryElem = adjustPage(queryElem, query.domainClass, pageable)
        }
        return queryElem.getAsResult().stream()
    }
}

/**
 * 삭제 쿼리를 실행하고, 삭제된 레코드 수를 반환합니다
 */
internal class CoroutineDeleteExecution(val operations: CoroutineRequeryOperations) : AbstractCoroutineQueryExecution() {
    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Int {
        logger.debug { "Execute delete query. query=$query, values=$values" }

        val deleteConditions = query.createQueryElement(values).whereElements

        val deleteQuery = operations
            .delete(query.domainKlass)
            .unwrap()
            .applyWhereConditions(deleteConditions)

        return deleteQuery.getAsScalarInt().value()
    }
}

/**
 * 엔티티 존재 여부를 확인하는 쿼리를 실행합니다.
 */
internal class CoroutineExistsExecution : AbstractCoroutineQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Boolean {
        logger.debug { "Execute exists query. query=$query, values=$values" }

        return query
            .createQueryElement(values)
            .limit(1)
            .getAsResult()
            .firstOrNull() != null
    }
}