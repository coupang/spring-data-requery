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

@file:JvmName("ExampleExtensions")

package org.springframework.data.requery.kotlin

import io.requery.query.Result
import io.requery.query.element.QueryElement
import mu.KotlinLogging
import org.springframework.data.domain.Example
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.repository.query.applyExample
import kotlin.reflect.KClass

private val log = KotlinLogging.logger { }

@Suppress("UNCHECKED_CAST")
fun <S : E, E : Any> Example<S>.buildQueryElement(operations: RequeryOperations, domainClass: KClass<E>): QueryElement<out Result<S>> {

    log.debug { "Build query element from Example. domainClass=$domainClass" }

    val root = operations.select(domainClass).unwrap()
    return root.applyExample(this) as QueryElement<out Result<S>>
}