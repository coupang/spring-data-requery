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

package org.springframework.data.requery.kotlin.core

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
import org.springframework.data.requery.kotlin.mapping.RequeryMappingContext
import kotlin.reflect.KClass

/**
 *  Java용 RequeryOperations
 *  // TODO: Move to Java code
 *
 * @author debop
 */
interface RequeryOperations {

    val dataStore: KotlinEntityDataStore<Any>
    val mappingContext: RequeryMappingContext

    @JvmDefault
    val entityModel: EntityModel
        get() = dataStore.getEntityModel()

    @JvmDefault
    val entityContext: EntityContext<Any>
        get() = dataStore.getEntityContext()

    @JvmDefault
    fun <E : Any> select(entityType: KClass<E>): Selection<out Result<E>> = dataStore.select(entityType)

    @JvmDefault
    fun <E : Any> select(entityType: KClass<E>, vararg attributes: QueryableAttribute<E, *>): Selection<out Result<E>> =
        dataStore.select(entityType, *attributes)

    @JvmDefault
    fun select(vararg expressions: Expression<*>): Selection<out Result<Tuple>> = dataStore.select(*expressions)

    @JvmDefault
    fun <E : Any, K> findById(entityType: KClass<E>, id: K): E? = dataStore.findByKey(entityType, id)

    @JvmDefault
    fun <E : Any> findAll(entityType: KClass<E>): List<E> = dataStore.select(entityType).get().toList()

    @JvmDefault
    fun <E : Any> refresh(entity: E): E = dataStore.refresh(entity)

    @JvmDefault
    fun <E : Any> refresh(entity: E, vararg attributes: Attribute<*, *>): E =
        dataStore.refresh(entity, *attributes)

    @JvmDefault
    fun <E : Any> refreshAll(entities: Iterable<E>, vararg attributes: Attribute<*, *>): List<E> =
        dataStore.refresh<E>(entities, *attributes).toList()

    @JvmDefault
    fun <E : Any> refreshAllProperties(entity: E): E = dataStore.refreshAll(entity)

    @JvmDefault
    fun <E : Any> refreshAllEntities(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> =
        entities.map { refreshAllProperties(it) }

    @JvmDefault
    fun <E : Any> upsert(entity: E): E = dataStore.upsert(entity)

    @JvmDefault
    fun <E : Any> upsertAll(entities: Iterable<E>): List<E> =
        dataStore.upsert<E>(entities).toList()

    @JvmDefault
    fun <E : Any> insert(entity: E): E = dataStore.insert(entity)

    @JvmDefault
    fun <E : Any, K : Any> insert(entity: E, keyClass: KClass<K>): K = dataStore.insert(entity, keyClass)

    @JvmDefault
    fun <E : Any> insert(entityType: KClass<E>): Insertion<out Result<Tuple>> = dataStore.insert<E>(entityType)

    @JvmDefault
    fun <E : Any> insert(entityType: KClass<E>, vararg attributes: QueryableAttribute<E, *>): InsertInto<out Result<Tuple>> =
        dataStore.insert(entityType, *attributes)

    @JvmDefault
    fun <E : Any> insertAll(entities: Iterable<E>): List<E> = dataStore.insert<E>(entities).toList()

    @JvmDefault
    fun <E : Any, K : Any> insertAll(entities: Iterable<E>, keyClass: KClass<K>): List<K> =
        dataStore.insert<K, E>(entities, keyClass).toList()

    @JvmDefault
    fun update(): Update<out Scalar<Int>> = dataStore.update()

    @JvmDefault
    fun <E : Any> update(entity: E): E = dataStore.update(entity)

    @JvmDefault
    fun <E : Any> update(entityType: KClass<E>): Update<out Scalar<Int>> = dataStore.update<E>(entityType)

    @JvmDefault
    fun <E : Any> updateAll(entities: Iterable<E>): List<E> = dataStore.update<E>(entities).toList()

    @JvmDefault
    fun <E : Any> delete(): Deletion<out Scalar<Int>> = dataStore.delete()

    @JvmDefault
    fun <E : Any> delete(entityType: KClass<E>): Deletion<out Scalar<Int>> = dataStore.delete<E>(entityType)

    @JvmDefault
    fun <E : Any> delete(entity: E) {
        dataStore.delete(entity)
    }

    @JvmDefault
    fun <E : Any> deleteAll(entities: Iterable<E>) {
        dataStore.delete<E>(entities)
    }

    @JvmDefault
    fun <E : Any> deleteAll(entityType: KClass<E>): Int = dataStore.delete<E>(entityType).get().value()

    @JvmDefault
    fun <E : Any> count(entityType: KClass<E>): Selection<out Scalar<Int>> =
        dataStore.count(entityType)

    @JvmDefault
    fun <E : Any> count(vararg attributes: QueryableAttribute<Any, *>): Selection<out Scalar<Int>> =
        dataStore.count(*attributes)


    @Suppress("UNCHECKED_CAST")
    @JvmDefault
    fun <E : Any> count(entityType: KClass<E>, whereClause: QueryElement<out Result<E>>): Int {

        val query = select(Count.count(entityType.java))
            .applyWhereConditions(whereClause.whereElements)
            .unwrapQuery() as QueryElement<out Result<Tuple>>

        val tuple = query.get().first()
        return tuple.get(0)
    }

    @JvmDefault
    fun <E : Any> exists(entityType: KClass<E>, whereClause: QueryElement<out Result<E>>): Boolean =
        whereClause.limit(1).get().firstOrNull() != null

    @JvmDefault
    fun raw(query: String, vararg parameters: Any): Result<Tuple> =
        dataStore.raw(query, *parameters)

    @JvmDefault
    fun <E : Any> raw(entityType: KClass<E>, query: String, vararg parameters: Any): Result<E> =
        dataStore.raw(entityType, query, *parameters)

    @JvmDefault
    fun <V> runInTransaction(callable: () -> V): V = runInTransaction(null, callable)

    @JvmDefault
    fun <V> runInTransaction(isolation: TransactionIsolation?, callable: () -> V): V =
        when(isolation) {
            null -> dataStore.withTransaction { callable.invoke() }
            else -> dataStore.withTransaction(isolation) { callable.invoke() }
        }

    @JvmDefault
    fun <V> withTransaction(block: (KotlinEntityDataStore<Any>) -> V): V = withTransaction(null, block)

    @JvmDefault
    fun <V> withTransaction(isolation: TransactionIsolation?, block: (KotlinEntityDataStore<Any>) -> V): V =
        when(isolation) {
            null -> dataStore.withTransaction { block.invoke(dataStore) }
            else -> dataStore.withTransaction(isolation) { block.invoke(dataStore) }
        }
}