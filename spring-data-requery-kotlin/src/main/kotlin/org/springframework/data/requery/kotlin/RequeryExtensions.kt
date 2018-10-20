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

import io.requery.BlockingEntityStore
import io.requery.meta.Attribute
import io.requery.meta.EntityModel
import io.requery.meta.Type
import io.requery.sql.EntityContext
import io.requery.sql.KotlinEntityDataStore
import mu.KotlinLogging
import org.springframework.util.ReflectionUtils
import kotlin.reflect.KClass

private val log = KotlinLogging.logger { }

/**
 * [BlockingEntityStore]에서 [EntityContext]를 추출합니다.
 * @param E entity type
 */
@Suppress("UNCHECKED_CAST")
@Throws(IllegalArgumentException::class)
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

/**
 * [KotlinEntityDataStore]에서 관리하는 [EntityModel] 정보를 추출합니다.
 * @param E entity type
 */
@Throws(IllegalArgumentException::class)
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

/**
 * [KotlinEntityDataStore]에서 관리하는 Entity의 정보를 추출합니다.
 * @param E entity type
 */
fun <E : Any> KotlinEntityDataStore<E>.getEntityTypes(): Set<Type<*>> {
    return this.getEntityModel().types
}

/**
 * [KotlinEntityDataStore]에서 관리하는 Entity의 수형을 추출합니다.
 * @param E entity type
 */
fun <E : Any> KotlinEntityDataStore<E>.getEntityClasses(): List<Class<*>> {
    return getEntityTypes().map { it.classType }
}

@Suppress("UNCHECKED_CAST")
fun <E : Any> KotlinEntityDataStore<*>.getType(entityClass: Class<E>): Type<E>? {
    return getEntityTypes().firstOrNull { entityClass == it.classType } as? Type<E>
}

@Suppress("UNCHECKED_CAST")
fun <E : Any> KotlinEntityDataStore<*>.getType(entityClass: KClass<E>): Type<E>? {
    return getEntityTypes().find { entityClass == it.classType.kotlin } as? Type<E>
}

/**
 * 지정한 엔티티 수형의 `Key` 속성에 해당하는 필드들의 정보를 가져옵니다.
 *
 * @param entityClass entity type
 */
fun <E : Any> KotlinEntityDataStore<*>.getKeyAttributes(entityClass: Class<E>): Set<Attribute<E, *>> {
    return getType(entityClass)?.keyAttributes ?: emptySet()
}

/**
 * 지정한 엔티티 수형의 `Key` 속성에 해당하는 필드들의 정보를 가져옵니다.
 *
 * @param entityKClass entity type
 */
fun <E : Any> KotlinEntityDataStore<*>.getKeyAttributes(entityKClass: KClass<E>): Set<Attribute<E, *>> {
    return getType(entityKClass)?.keyAttributes ?: emptySet()
}


/**
 * 지정한 엔티티 수형의 `Key` 속성에 해당하는 유일한 필드의 정보를 가져옵니다.
 *
 * @param entityClass entity type
 */
fun <E : Any> KotlinEntityDataStore<*>.getSingleKeyAttribute(entityClass: Class<E>): Attribute<E, *> {
    return getType(entityClass)?.singleKeyAttribute!!
}

/**
 * 지정한 엔티티 수형의 `Key` 속성에 해당하는 유일한 필드의 정보를 가져옵니다.
 *
 * @param entityKClass entity type
 */
fun <E : Any> KotlinEntityDataStore<*>.getSingleKeyAttribute(entityKClass: KClass<E>): Attribute<E, *> {
    return getType(entityKClass)?.singleKeyAttribute!!
}
