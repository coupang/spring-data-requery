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

package org.springframework.data.requery.kotlin.repository

import io.requery.query.Condition
import io.requery.query.Result
import io.requery.query.Return
import io.requery.query.element.QueryElement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * Coroutine을 이용하여 Requery 조건에 해당하는 질의를 수행하는 Interface
 *
 * @author debop
 * @since 18. 10. 14
 */
interface CoroutineRequeryConditionExecutor<E : Any> {

    suspend fun findOne(filter: Return<out Result<E>>): E?

    suspend fun findAll(filter: Return<out Result<E>>, sort: Sort = Sort.unsorted()): List<E>

    suspend fun findAll(filter: Return<out Result<E>>, pageable: Pageable): Page<E>

    suspend fun findAll(conditions: Iterable<Condition<E, *>>, sort: Sort = Sort.unsorted()): List<E>

    suspend fun findAll(conditions: Iterable<Condition<E, *>>, pageable: Pageable): Page<E>

    suspend fun count(queryElement: QueryElement<out Result<E>>): Long

    suspend fun exists(queryElement: QueryElement<out Result<E>>): Boolean

}