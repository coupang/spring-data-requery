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

@file:JvmName("RequeryExtensions")

package org.springframework.data.requery.kotlin

import io.requery.meta.Attribute
import io.requery.meta.EntityModel
import io.requery.meta.Type
import io.requery.sql.EntityContext
import io.requery.sql.KotlinEntityDataStore
import mu.KotlinLogging
import org.springframework.util.ReflectionUtils
import kotlin.reflect.KClass

private val log = KotlinLogging.logger { }

@Suppress("UNCHECKED_CAST")
fun <E : Any> KotlinEntityDataStore<E>.getEntityContext(): EntityContext<E> {
    try {
        val dataStore = this.data
        val field = ReflectionUtils.findField(dataStore.javaClass, "context")!!
        field.isAccessible = true

        return ReflectionUtils.getField(field, dataStore) as EntityContext<E>
    } catch(e: Exception) {
        throw IllegalArgumentException("Fail to retrieve EntityContext.", e)
    }
}

fun <E : Any> KotlinEntityDataStore<E>.getEntityModel(): EntityModel {
    try {
        val dataStore = this.data
        val field = ReflectionUtils.findField(dataStore.javaClass, "entityModel")!!
        field.isAccessible = true

        return ReflectionUtils.getField(field, dataStore) as EntityModel
    } catch(e: Exception) {
        throw IllegalArgumentException("Fail to retrieve EntityModel", e)
    }
}

fun <E : Any> KotlinEntityDataStore<E>.getEntityTypes(): Set<Type<*>> {
    return this.getEntityModel().types
}

fun <E : Any> KotlinEntityDataStore<E>.getEntityClasses(): List<Class<*>> {
    return getEntityTypes().map { it.classType }
}

@Suppress("UNCHECKED_CAST")
fun <E : Any> KotlinEntityDataStore<*>.getType(entityClass: Class<E>): Type<E>? {
    return getEntityTypes().find { entityClass == it.classType } as? Type<E>
}

@Suppress("UNCHECKED_CAST")
fun <E : Any> KotlinEntityDataStore<*>.getType(entityClass: KClass<E>): Type<E>? {
    return getEntityTypes().find { entityClass.java == it.classType } as? Type<E>
}

fun <E : Any> KotlinEntityDataStore<*>.getKeyAttributes(entityClass: Class<E>): Set<Attribute<E, *>> {
    return getType(entityClass)?.keyAttributes ?: emptySet()
}

fun <E : Any> KotlinEntityDataStore<*>.getSingleKeyAttribute(entityClass: Class<E>): Attribute<E, *> {
    return getType(entityClass)?.singleKeyAttribute!!
}

