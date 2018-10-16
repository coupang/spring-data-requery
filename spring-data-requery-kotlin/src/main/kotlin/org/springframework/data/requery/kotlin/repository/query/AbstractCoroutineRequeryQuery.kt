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
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import mu.KLogging
import org.springframework.data.repository.query.QueryMethod
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.repository.support.RequeryMetamodel
import kotlin.reflect.KClass

/**
 * Abstract base class to implement Requery [RepositoryQuery]s with Coroutine
 *
 * @author debop (Sunghyouk Bae)
 */
abstract class AbstractCoroutineRequeryQuery(val queryMethod: RequeryQueryMethod,
                                             val operations: CoroutineRequeryOperations) : RepositoryQuery {

    companion object : KLogging()

    val metamodel = RequeryMetamodel(operations.entityModel)
    val domainKlass: KClass<out Any> = queryMethod.entityInformation.kotlinType
    val domainClass: Class<out Any> = queryMethod.entityInformation.javaType
    val domainClassName: String = domainClass.simpleName

    override fun getQueryMethod(): QueryMethod = queryMethod

    override fun execute(parameters: Array<Any>): Any? =
        runBlocking {
            GlobalScope.async { doExecute(getExecution(), parameters) }.await()
        }

    private suspend fun doExecute(execution: AbstractCoroutineQueryExecution, values: Array<Any>): Any? {
        val result = execution.execute(this, values)
        logger.trace { "doExecute ... result=[$result]" }

        return result
    }

    protected open fun getExecution(): AbstractCoroutineQueryExecution {
        TODO("Not implemented")
    }

    protected abstract suspend fun doCreateQuery(values: Array<Any>): QueryElement<out Any>

    protected abstract suspend fun doCreateCountQuery(values: Array<Any>): QueryElement<out Result<Int>>

    internal suspend fun createQueryElement(values: Array<Any>): QueryElement<out Any> {
        logger.debug { "Create QueryElement with domainClass=${domainClass.name}, values=$values" }
        return doCreateQuery(values)
    }

    protected suspend fun createCountQueryElement(values: Array<Any>): QueryElement<out Result<Int>> {
        return doCreateCountQuery(values)
    }

    protected fun getTypeToRead(returnedType: ReturnedType): Class<*>? {
        val isProjection = returnedType.isProjecting && !metamodel.isRequeryManaged(returnedType.returnedType)

        return if(isProjection) Tuple::class.java else null
    }
}