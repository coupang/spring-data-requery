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

package org.springframework.data.requery.kotlin.coroutines

import io.requery.TransactionIsolation
import io.requery.kotlin.Deletion
import io.requery.kotlin.InsertInto
import io.requery.kotlin.Insertion
import io.requery.kotlin.QueryableAttribute
import io.requery.kotlin.Selection
import io.requery.kotlin.Update
import io.requery.meta.Attribute
import io.requery.meta.EntityModel
import io.requery.query.Expression
import io.requery.query.Result
import io.requery.query.Scalar
import io.requery.query.Tuple
import io.requery.query.element.QueryElement
import io.requery.query.function.Count
import io.requery.sql.EntityContext
import io.requery.sql.KotlinEntityDataStore
import org.springframework.data.requery.kotlin.applyWhereConditions
import org.springframework.data.requery.kotlin.getEntityContext
import org.springframework.data.requery.kotlin.getEntityModel
import kotlin.reflect.KClass

/**
 * Kotlin Coroutines 환경 하에서 실행되는 RequeryOperations 입니다.

 * @author debop
 */
interface CoroutineRequeryOperations {

    val dataStore: KotlinEntityDataStore<Any>

    @JvmDefault
    val entityModel: EntityModel
        get() = dataStore.getEntityModel()

    @JvmDefault
    val entityContext: EntityContext<out Any>
        get() = dataStore.getEntityContext()

    @JvmDefault
    suspend infix fun <T : Any> select(entityType: KClass<T>): Selection<out Result<T>> =
        dataStore.select(entityType)

    @JvmDefault
    suspend fun <T : Any> select(entityType: KClass<T>, vararg attributes: QueryableAttribute<T, *>): Selection<out Result<T>> =
        dataStore.select(entityType, *attributes)

    @JvmDefault
    suspend fun select(vararg expressions: Expression<*>): Selection<Result<Tuple>> =
        dataStore.select(*expressions)

    @JvmDefault
    suspend fun <T : Any, ID> findById(entityType: KClass<T>, id: ID): T? =
        dataStore.findByKey(entityType, id)

    @JvmDefault
    suspend infix fun <T : Any> findAll(entityType: KClass<T>): List<T> =
        dataStore.select(entityType).get().toList()

    @JvmDefault
    suspend infix fun <T : Any> refresh(entity: T): T =
        dataStore.refresh(entity)

    @JvmDefault
    suspend fun <T : Any> refresh(entity: T, vararg attributes: Attribute<*, *>): T =
        dataStore.refresh(entity, *attributes)

    @JvmDefault
    suspend fun <T : Any> refreshAll(entities: Iterable<T>, vararg attributes: Attribute<*, *>): List<T> =
        dataStore.refresh<T>(entities, *attributes).toList()

    @JvmDefault
    suspend fun <T : Any> refreshAllProperties(entity: T): T =
        dataStore.refreshAll(entity)

    @JvmDefault
    suspend fun <T : Any> refreshAllEntities(entities: Iterable<T>, vararg attributes: Attribute<T, *>): List<T> =
        entities.map { refreshAllProperties(it) }

    @JvmDefault
    suspend infix fun <T : Any> upsert(entity: T): T =
        dataStore.upsert(entity)

    @JvmDefault
    suspend infix fun <T : Any> upsertAll(entities: Iterable<T>): List<T> {
        if(entities.none())
            return emptyList()

        return dataStore.upsert<T>(entities).toList()
    }

    @JvmDefault
    suspend infix fun <T : Any> insert(entity: T): T =
        dataStore.insert(entity)

    suspend fun <T : Any, K : Any> insert(entity: T, keyKlass: KClass<K>): K =
        dataStore.insert(entity, keyKlass)

    @JvmDefault
    suspend infix fun <T : Any> insert(entityType: KClass<T>): Insertion<Result<Tuple>> =
        dataStore.insert<T>(entityType)

    @JvmDefault
    suspend fun <T : Any> insert(entityType: KClass<T>, vararg attributes: QueryableAttribute<T, *>): InsertInto<out Result<Tuple>> =
        dataStore.insert(entityType, *attributes)

    @JvmDefault
    suspend infix fun <T : Any> insertAll(entities: Iterable<T>): List<T> =
        dataStore.insert<T>(entities).toList()

    @JvmDefault
    suspend fun <T : Any, K : Any> insertAll(entities: Iterable<T>, keyKlass: KClass<K>): List<K> =
        dataStore.insert<K, T>(entities, keyKlass).toList()

    @JvmDefault
    suspend fun <T : Any> update(): Update<Scalar<Int>> =
        dataStore.update()

    @JvmDefault
    suspend infix fun <T : Any> update(entity: T): T =
        dataStore.update(entity)

    @JvmDefault
    suspend infix fun <T : Any> update(entityType: KClass<T>): Update<Scalar<Int>> =
        dataStore.update<T>(entityType)

    @JvmDefault
    suspend infix fun <T : Any> updateAll(entities: Iterable<T>): List<T> =
        dataStore.update<T>(entities).toList()

    @JvmDefault
    suspend fun delete(): Deletion<Scalar<Int>> =
        dataStore.delete()

    @JvmDefault
    suspend infix fun <T : Any> delete(entity: T) {
        dataStore.delete(entity)
    }

    @JvmDefault
    suspend infix fun <T : Any> delete(entityType: KClass<T>): Deletion<Scalar<Int>> =
        dataStore.delete<T>(entityType)

    @JvmDefault
    suspend infix fun <T : Any> deleteAll(entities: Iterable<T>) {
        dataStore.delete<T>(entities)
    }

    @JvmDefault
    suspend infix fun <T : Any> deleteAll(entityType: KClass<T>): Int =
        dataStore.delete<T>(entityType).get().value()

    @JvmDefault
    suspend infix fun <T : Any> count(entityType: KClass<T>): Selection<Scalar<Int>> =
        dataStore.count(entityType)

    @JvmDefault
    suspend fun <E : Any> count(vararg attributes: QueryableAttribute<Any, *>): Selection<out Scalar<Int>> =
        dataStore.count(*attributes)


    @Suppress("UNCHECKED_CAST")
    @JvmDefault
    suspend fun <E : Any> count(entityType: KClass<E>, whereClause: QueryElement<out Result<E>>): Int {

        val query = select(Count.count(entityType.java))
            .applyWhereConditions(whereClause.whereElements)
            .unwrapQuery() as QueryElement<out Result<Tuple>>

        val tuple = query.get().first()
        return tuple.get(0)
    }

    @JvmDefault
    suspend fun <E : Any> exists(entityType: KClass<E>, whereClause: QueryElement<out Result<E>>): Boolean =
        whereClause.limit(1).get().firstOrNull() != null

    @JvmDefault
    suspend fun raw(query: String, vararg parameters: Any): Result<Tuple> =
        dataStore.raw(query, *parameters)

    @JvmDefault
    suspend fun <T : Any> raw(entityType: KClass<T>, query: String, vararg parameters: Any): Result<T> =
        dataStore.raw(entityType, query, *parameters)

    @JvmDefault
    suspend fun <T : Any> withTransaction(block: CoroutineRequeryOperations.() -> T): T =
        withTransaction(null, block)

    suspend fun <T : Any> withTransaction(isolation: TransactionIsolation?, block: CoroutineRequeryOperations.() -> T): T

    @JvmDefault
    suspend fun <T : Any> withDataStore(block: KotlinEntityDataStore<Any>.() -> T): T =
        block.invoke(dataStore)

}