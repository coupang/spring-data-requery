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
import mu.KLogging
import org.springframework.data.repository.query.QueryMethod
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations

/**
 * CoroutinePartTreeRequerQuery
 * @author debop (Sunghyouk Bae)
 */
class CoroutinePartTreeRequerQuery(queryMethod: RequeryQueryMethod,
                                   operations: CoroutineRequeryOperations)
    : AbstractCoroutineRequeryQuery(queryMethod, operations) {

    companion object : KLogging()

    //    private val tree: PartTree
    //    private val paramters: RequeryParameters
    //
    //    private val countQueryPreparer: CountQueryPreparer
    //    private val queryPreparer: QueryPreparer
    //    private val context: RequeryMappingContext

    init {
        val parameters = queryMethod.parameters

        TODO("Not implemented")
    }

    override fun getQueryMethod(): QueryMethod {
        TODO("not implemented")
    }

    override fun execute(parameters: Array<Any>): Any? {
        TODO("not implemented")
    }

    override suspend fun doCreateQuery(values: Array<Any>): QueryElement<out Any> {
        TODO("not implemented")
    }

    override suspend fun doCreateCountQuery(values: Array<Any>): QueryElement<out Result<Int>> {
        TODO("not implemented")
    }

}