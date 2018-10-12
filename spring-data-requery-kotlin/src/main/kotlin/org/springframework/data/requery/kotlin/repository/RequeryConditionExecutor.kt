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
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * org.springframework.data.requery.kotlin.repository.RequeryConditionExecutor
 *
 * @author debop
 */
@NoRepositoryBean
interface RequeryConditionExecutor<E : Any> {

    fun findOne(filter: Return<out Result<E>>): Optional<E>

    fun findAll(filter: Return<out Result<E>>): List<E>

    fun findAll(filter: QueryElement<out Result<E>>, pageable: Pageable): Page<E>

    fun findAll(conditions: Iterable<Condition<E, *>>): List<E>

    fun findAll(conditions: Iterable<Condition<E, *>>, pageable: Pageable): Page<E>

    fun findAll(conditions: Iterable<Condition<E, *>>, sort: Sort): List<E>


    fun count(queryElement: QueryElement<out Result<E>>): Long

    fun exists(queryElement: QueryElement<out Result<E>>): Boolean
}