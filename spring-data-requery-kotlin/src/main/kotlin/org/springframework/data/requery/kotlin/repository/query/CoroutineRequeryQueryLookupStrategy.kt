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
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryLookupStrategy.Key.CREATE
import org.springframework.data.repository.query.QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND
import org.springframework.data.repository.query.QueryLookupStrategy.Key.USE_DECLARED_QUERY
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.data.requery.kotlin.annotation.Query
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import java.lang.reflect.Method

/**
 * Query lookup strategy to execute finders with coroutine
 *
 * @author debop (Sunghyouk Bae)
 */
object CoroutineRequeryQueryLookupStrategy : KLogging() {

    fun create(operations: CoroutineRequeryOperations,
               key: QueryLookupStrategy.Key?,
               evaluationContextProvider: QueryMethodEvaluationContextProvider): QueryLookupStrategy {
        logger.debug { "Create QueryLookupStrategy with key=$key" }

        return when(key ?: CREATE_IF_NOT_FOUND) {
            CREATE              -> CoroutineCreateQueryLookupStrategy(operations)
            USE_DECLARED_QUERY  -> CoroutineDeclaredQueryLookupStrategy(operations, evaluationContextProvider)
            CREATE_IF_NOT_FOUND ->
                CoroutineCreateIfNotFoundQueryLookupStrategy(operations,
                                                             CoroutineCreateQueryLookupStrategy(operations),
                                                             CoroutineDeclaredQueryLookupStrategy(operations, evaluationContextProvider))
            else                ->
                throw IllegalArgumentException("Unsupported query lookup strategy. key=$key")
        }
    }

    /**
     * Base class for [QueryLookupStrategy] implementations that need access to an [CoroutineRequeryOperations].
     */
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

    /**
     * [QueryLookupStrategy] to create a query from the queryMethod name.
     */
    class CoroutineCreateQueryLookupStrategy(operations: CoroutineRequeryOperations)
        : AbstractCoroutineQueryLookupStrategy(operations) {
        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: CoroutineRequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {
            logger.debug { "Create PartTreeRequeryQuery for queryMethod [${method.name}]" }

            return CoroutinePartTreeRequerQuery(method, operations)
        }
    }

    /**
     * [QueryLookupStrategy] that tries to detect a declared query declared via [Query] annotation followed by
     * a Requery named query lookup.
     */
    @Suppress("UNUSED_PARAMETER")
    class CoroutineDeclaredQueryLookupStrategy(operations: CoroutineRequeryOperations,
                                               evaluationContextProvider: QueryMethodEvaluationContextProvider)
        : AbstractCoroutineQueryLookupStrategy(operations) {

        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: CoroutineRequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {
            // @Query annotation이 있다면 그 값으로 한다
            if(method.isAnnotatedQuery) {
                logger.debug { "Create DeclaredRequeryQuery for @Query annotated queryMethod. queryMethod [${method.name}" }
                return CoroutineDeclaredRequeryQuery(method, operations)
            }

            // TODO: spring-data-jpa 처럼 NamedQuery 를 만들어서 제공해야 한다. (spring-data-jdbc 를 활용하자)
            // NOTE: NamedQuery 가 하는 일이 Custom implemented queryMethod 에 대한 수행을 담당한다. ㅠ.ㅠ
            error("Cannot find a annotated query for queryMethod [$method]")
        }
    }

    /**
     * [QueryLookupStrategy] to try to detect a declared query first
     * ([org.springframework.data.requery.kotlin.annotation.Query], Requery named query).
     * In case none is found we fall back on query creation.
     */
    class CoroutineCreateIfNotFoundQueryLookupStrategy(operations: CoroutineRequeryOperations,
                                                       val createStrategy: CoroutineCreateQueryLookupStrategy,
                                                       val declaredStrategy: CoroutineDeclaredQueryLookupStrategy)
        : AbstractCoroutineQueryLookupStrategy(operations) {

        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: CoroutineRequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {
            logger.debug { "Resolve query by CoroutineDeclaredQueryLookupStrategy ..." }
            return try {
                declaredStrategy.resolveQuery(method, operations, namedQueries)
            } catch(se: IllegalStateException) {
                logger.debug { "Resolve query by CoroutineCreateQueryLookupStrategy ..." }
                createStrategy.resolveQuery(method, operations, namedQueries)
            }
        }
    }
}