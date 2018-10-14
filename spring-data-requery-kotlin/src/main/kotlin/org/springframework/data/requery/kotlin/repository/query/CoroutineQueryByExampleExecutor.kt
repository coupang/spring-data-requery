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

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

/**
 * Interface to allow couroutine execution of Query by Example {@link Example} instances.
 *
 * @author debop
 * @since 18. 10. 14
 */
interface CoroutineQueryByExampleExecutor<E : Any> {

    /**
     * Returns a single entity matching the given [Example] or null if none was found.
     *
     * @param example must not be null.
     * @return a single entity matching the given [Example] or [Optional.empty] if none was found.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the Example yields more than one result.
     */
    suspend fun <S : E> findOne(example: Example<S>): S?

    /**
     * Returns all entities matching the given [Example] applying the given [Sort]. In case no match could be
     * found an empty [Iterable] is returned.
     *
     * @param example must not be null.
     * @param sort the [Sort] specification to sort the results by, must not be null.
     * @return all entities matching the given [Example].
     * @since 1.10
     */
    suspend fun <S : E> findAll(example: Example<S>, sort: Sort = Sort.unsorted()): Iterable<S>

    /**
     * Returns a [Page] of entities matching the given [Example]. In case no match could be found, an empty
     * [Page] is returned.
     *
     * @param example must not be null.
     * @param pageable can be null.
     * @return a [Page] of entities matching the given [Example].
     */
    suspend fun <S : E> findAll(example: Example<S>, pageable: Pageable): Page<S>

    /**
     * Returns the number of instances matching the given [Example].
     *
     * @param example the [Example] to count instances for. Must not be null.
     * @return the number of instances matching the [Example].
     */
    suspend fun <S : E> count(example: Example<S>): Long

    /**
     * Checks whether the data store contains elements that match the given [Example].
     *
     * @param example the [Example] to use for the existence check. Must not be null.
     * @return true if the data store contains elements that match the given [Example].
     */
    suspend fun <S : E> exists(example: Example<S>): Boolean
}