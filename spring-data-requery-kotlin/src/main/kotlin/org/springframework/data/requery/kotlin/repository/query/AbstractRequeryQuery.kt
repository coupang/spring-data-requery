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
import io.requery.query.Tuple
import io.requery.query.element.QueryElement
import mu.KotlinLogging
import org.springframework.data.repository.query.QueryMethod
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.utils.RequeryMetamodel
import kotlin.reflect.KClass

/**
 * Abstract base class to implement [RepositoryQuery]s.
 *
 * @author debop
 */
abstract class AbstractRequeryQuery(val queryMethod: RequeryQueryMethod,
                                    val operations: RequeryOperations) : RepositoryQuery {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    val metamodel = RequeryMetamodel(operations.entityModel)
    val domainKlass: KClass<out Any> = queryMethod.entityInformation.kotlinType
    val domainClass: Class<out Any> = queryMethod.entityInformation.javaType

    override fun getQueryMethod(): QueryMethod = queryMethod

    override fun execute(parameters: Array<Any>): Any? = doExecute(getExecution(), parameters)

    private fun doExecute(execution: RequeryQueryExecution, values: Array<Any>): Any? {

        val result = execution.execute(this, values)
        // 필요없는데 ???
        //        val accessor = ParamtersParameterAccessor(queryMethod.parameters, values)

        log.debug { "doExecute ... result=[$result]" }
        return result
    }

    protected open fun getExecution(): RequeryQueryExecution {

        return when {
            queryMethod.isStreamQuery -> StreamExecution(queryMethod.parameters)
            queryMethod.isCollectionQuery -> CollectionExecution()
            queryMethod.isSliceQuery -> SlicedExecution(queryMethod.parameters)
            queryMethod.isPageQuery -> PagedExecution(queryMethod.parameters)
            else -> SingleEntityExecution()
        }
    }

    protected abstract fun doCreateQuery(values: Array<Any>): QueryElement<out Any>

    protected abstract fun doCreateCountQuery(values: Array<Any>): QueryElement<out Result<Int>>

    internal fun createQueryElement(values: Array<Any>): QueryElement<out Any> {
        log.debug { "Create QueryElement with domainClass=${domainClass.name}, values=$values" }
        return doCreateQuery(values)
    }

    protected fun createCountQueryElement(values: Array<Any>): QueryElement<out Result<Int>> {
        return doCreateCountQuery(values)
    }

    protected fun getTypeToRead(returnedType: ReturnedType): Class<*>? {
        return when {
            returnedType.isProjecting && !metamodel.isRequeryManaged(returnedType.returnedType) -> Tuple::class.java
            else -> null
        }
    }
}