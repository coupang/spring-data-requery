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

import mu.KotlinLogging
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
import org.springframework.data.requery.kotlin.core.RequeryOperations
import java.lang.reflect.Method

/**
 * Query lookup strategy to execute finders.
 *
 * @author debop
 */
object RequeryQueryLookupStrategy {

    private val log = KotlinLogging.logger { }

    @JvmStatic
    fun create(operations: RequeryOperations,
               key: QueryLookupStrategy.Key?,
               evaluationContextProvider: QueryMethodEvaluationContextProvider): QueryLookupStrategy {

        log.debug { "Create QueryLookupStrategy with key=$key" }

        return when(key ?: CREATE_IF_NOT_FOUND) {
            CREATE              -> {
                CreateQueryLookupStrategy(operations)
            }
            USE_DECLARED_QUERY  -> {
                DeclaredQueryLookupStrategy(operations, evaluationContextProvider)
            }
            CREATE_IF_NOT_FOUND -> {
                CreateIfNotFoundQueryLookupStrategy(operations,
                                                    CreateQueryLookupStrategy(operations),
                                                    DeclaredQueryLookupStrategy(operations, evaluationContextProvider))
            }
            else                ->
                throw IllegalArgumentException("Unsupported query lookup strategy. key=$key")
        }
    }

    /**
     * Base class for [QueryLookupStrategy] implementations that need access to an [RequeryOperations].
     */
    abstract class AbstractQueryLookupStrategy(val operations: RequeryOperations) : QueryLookupStrategy {

        override fun resolveQuery(method: Method,
                                  metadata: RepositoryMetadata,
                                  factory: ProjectionFactory,
                                  namedQueries: NamedQueries): RepositoryQuery {
            return resolveQuery(RequeryQueryMethod(method, metadata, factory),
                                operations,
                                namedQueries)
        }

        abstract fun resolveQuery(method: RequeryQueryMethod,
                                  operations: RequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery
    }

    /**
     *  [QueryLookupStrategy] to create a query from the queryMethod name.
     */
    class CreateQueryLookupStrategy(operations: RequeryOperations) : AbstractQueryLookupStrategy(operations) {

        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: RequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {
            log.debug { "Create PartTreeRequeryQuery for queryMethod [$method]" }

            return PartTreeRequeryQuery(method, operations)
        }
    }

    /**
     * [QueryLookupStrategy] that tries to detect a declared query declared via [Query] annotation followed by
     * a Requery named query lookup.
     */
    @Suppress("UNUSED_PARAMETER")
    class DeclaredQueryLookupStrategy(operations: RequeryOperations,
                                      evaluationContextProvider: QueryMethodEvaluationContextProvider) : AbstractQueryLookupStrategy(operations) {

        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: RequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {

            // @Query annotation이 있다면 그 값으로 한다
            if(method.isAnnotatedQuery) {
                log.debug { "Create DeclaredRequeryQuery for @Query annotated queryMethod. queryMethod [${method.name}" }
                return DeclaredRequeryQuery(method, operations)
            }

            // TODO: spring-data-jpa 처럼 NamedQuery 를 만들어서 제공해야 한다. (spring-data-jdbc 를 활용하자)
            // NOTE: NamedQuery 가 하는 일이 Custom implemented queryMethod 에 대한 수행을 담당한다. ㅠ.ㅠ
            error("Cannot find a annotated query for queryMethod [$method]")
        }
    }

    /**
     * [QueryLookupStrategy] to try to detect a declared query first ([org.springframework.data.requery.kotlin.annotation.Query], Requery named query).
     * In case none is found we fall back on query creation.
     */
    class CreateIfNotFoundQueryLookupStrategy(operations: RequeryOperations,
                                              val createStrategy: CreateQueryLookupStrategy,
                                              val declaredStrategy: DeclaredQueryLookupStrategy) : AbstractQueryLookupStrategy(operations) {

        override fun resolveQuery(method: RequeryQueryMethod,
                                  operations: RequeryOperations,
                                  namedQueries: NamedQueries): RepositoryQuery {
            return try {
                log.debug { "Resolve query by DeclaredQueryLookupStrategy ..." }
                declaredStrategy.resolveQuery(method, operations, namedQueries)
            } catch(se: IllegalStateException) {
                log.debug { "Resolve query by CreateQueryLookupStrategy ..." }
                createStrategy.resolveQuery(method, operations, namedQueries)
            }
        }

    }
}