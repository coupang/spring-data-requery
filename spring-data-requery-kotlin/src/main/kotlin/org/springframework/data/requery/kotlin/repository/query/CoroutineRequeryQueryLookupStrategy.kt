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

import mu.KLogging
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.NamedQueries
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.EvaluationContextProvider
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import java.lang.reflect.Method

/**
 * CoroutineRequeryQueryLookupStrategy
 * @author debop (Sunghyouk Bae)
 */
object CoroutineRequeryQueryLookupStrategy : KLogging() {

    fun create(operations: CoroutineRequeryOperations,
               key: QueryLookupStrategy.Key?,
               evaluationContextProvider: EvaluationContextProvider): QueryLookupStrategy {
        logger.debug { "Create QueryLookupStrategy with key=$key" }

        TODO("Not implemented")
    }


    abstract class AbstractCoroutineQueryLookupStrategy(val operations: CoroutineRequeryOperations) : QueryLookupStrategy {
        override fun resolveQuery(method: Method,
                                  metadata: RepositoryMetadata,
                                  factory: ProjectionFactory,
                                  namedQueries: NamedQueries): RepositoryQuery {
            return resolveQuery(RequeryQueryMethod(method, metadata, factory),
                                operations,
                                namedQueries)
        }

        abstract fun resolveQuery(method: RequeryQueryMethod,
                                  operations: CoroutineRequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery
    }

    class CreateQueryLookupStrategy(operations: CoroutineRequeryOperations) : AbstractCoroutineQueryLookupStrategy(operations) {
        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: CoroutineRequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {
            logger.debug { "Create PartTreeRequeryQuery for queryMethod [${method.name}]" }

            return CoroutinePartTreeRequerQuery(method, operations)
        }
    }

    // TODO: 구현 중 
}