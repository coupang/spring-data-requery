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
import mu.KotlinLogging
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.ConfigurableConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl
import org.springframework.data.repository.query.ParametersParameterAccessor
import org.springframework.data.requery.kotlin.applyPageable
import org.springframework.data.requery.kotlin.applyWhereConditions
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.getAsResult
import org.springframework.data.requery.kotlin.getAsScalarInt
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.util.ClassUtils

/**
 * RequeryQueryExecution
 *
 * @author debop
 */
abstract class RequeryQueryExecution {

    companion object {

        private val log = KotlinLogging.logger { }

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

    fun execute(query: AbstractRequeryQuery, values: Array<Any>): Any? {

        return try {
            doExecute(query, values)
        } catch(pe: PersistenceException) {
            log.error(pe) { "Fail to doExecute. query=$query" }
            return null
        }
    }

    protected abstract fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Any?


    /**
     * method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
     */
    protected fun adjustPage(queryElement: QueryElement<out Any>,
                             domainClass: Class<out Any>,
                             pageable: Pageable): QueryElement<out Any> {

        // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
        return if(queryElement.limit == null && queryElement.offset == null)
            queryElement.applyPageable(domainClass, pageable).unwrap()
        else
            queryElement
    }
}


internal class CollectionExecution : RequeryQueryExecution() {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): List<*> {
        val result = query.createQueryElement(values).get() as Result<*>
        return result.toList()
    }
}

internal class SlicedExecution(val parameters: RequeryParameters) : RequeryQueryExecution() {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Any? {
        log.debug { "execute requery query, then return sliced list" }
        val accessor = ParametersParameterAccessor(parameters, values)
        val pageable = accessor.pageable

        var queryElement = query.createQueryElement(values)

        return when {
            pageable.isPaged -> {
                // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
                if(queryElement.limit == null) {
                    queryElement = queryElement.applyPageable(query.domainClass, pageable)
                }
                val minLimit = minOf(queryElement.limit, pageable.pageSize)
                queryElement = queryElement.offset(pageable.pageNumber * minLimit).unwrap()

                val pageSize = queryElement.limit
                queryElement = queryElement.limit(pageSize + 1).unwrap()

                log.trace { "offset=${queryElement.offset}, limit=${queryElement.limit}, pageSize=$pageSize" }

                val result = queryElement.getAsResult()
                val resultList = result.toList()
                val hasNext = resultList.size > pageSize

                SliceImpl(if(hasNext) resultList.subList(0, pageSize) else resultList,
                          pageable,
                          hasNext)
            }
            else -> {
                val result = queryElement.getAsResult()
                SliceImpl(result.toList())
            }
        }
    }
}

internal class PagedExecution(val parameters: RequeryParameters) : RequeryQueryExecution() {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Page<*>? {
        log.debug { "Run paged exection... query=$query, values=$values" }

        val accessor = ParametersParameterAccessor(parameters, values)
        val pageable = accessor.pageable

        var queryElement = query.createQueryElement(values)

        return when {
            pageable.isPaged -> {
                // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
                queryElement = adjustPage(queryElement, query.domainClass, pageable)
                log.trace { "offet=${queryElement.offset}, limit=${queryElement.limit}, pageable=$pageable" }

                val result = queryElement.getAsResult().toList()
                val totals = doExecuteTotals(query, values)

                log.trace { "Result size=${result.size}, totals=$totals" }
                PageImpl(result, pageable, totals)
            }
            else -> {
                PageImpl(queryElement.getAsResult().toList())
            }
        }
    }

    private fun doExecuteTotals(query: AbstractRequeryQuery, values: Array<Any>): Long {

        val queryElement = query.createQueryElement(values).unwrap()
        val selection = query.operations.select(Count.count(query.domainClass))

        selection.unwrap().whereElements.addAll(queryElement.whereElements)

        val result = RequeryResultConverter.convert(selection.get().firstOrNull(), 0L) as Int
        return result.toLong()
    }
}

internal class SingleEntityExecution : RequeryQueryExecution() {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Any? {
        log.debug { "Get single entity. query=$query, values=$values" }
        val value = query.createQueryElement(values).getAsResult().firstOrNull()
        return RequeryResultConverter.convert(value)
    }
}

/**
 *  [RequeryQueryExecution] executing a Java 8 Stream.
 */
internal class StreamExecution(val parameters: RequeryParameters) : RequeryQueryExecution() {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Any? {
        log.debug { "Get stream of entities. query=$query, values=$values" }

        val accessor = ParametersParameterAccessor(parameters, values)
        val pageable = accessor.pageable

        var queryElement = query.createQueryElement(values)

        return when {
            pageable.isPaged -> {
                // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
                queryElement = adjustPage(queryElement, query.domainClass, pageable)
                queryElement.getAsResult().stream()
            }
            else ->
                queryElement.getAsResult().stream()
        }

    }
}

internal class DeleteExecution(val operations: RequeryOperations) : RequeryQueryExecution() {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Any? {
        log.debug { "execute Delete query. query=$query, values=$values" }

        val deleteConditions = query.createQueryElement(values).whereElements

        val deleteQuery = operations
            .delete(query.domainKlass)
            .unwrap()
            .applyWhereConditions(deleteConditions)

        return deleteQuery.getAsScalarInt().value()
    }
}

internal class ExistsExecution : RequeryQueryExecution() {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun doExecute(query: AbstractRequeryQuery, values: Array<Any>): Any? {
        // TODO: Entity를 Load하는 것과 Count를 세는 것의 성능 차이 비교가 필요하다.
        log.debug { "execute Exists query. query=$query, values=$values" }
        return query
            .createQueryElement(values)
            .limit(1)
            .getAsResult()
            .firstOrNull() != null
    }

}