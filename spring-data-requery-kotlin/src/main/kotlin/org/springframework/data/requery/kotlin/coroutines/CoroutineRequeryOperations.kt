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

    val entityStore: CoroutineEntityStore<Any>

    @JvmDefault
    val entityModel: EntityModel
        get() = entityStore.dataStore.getEntityModel()

    @JvmDefault
    val entityContext: EntityContext<out Any>
        get() = entityStore.dataStore.getEntityContext()

    @JvmDefault
    suspend infix fun <T : Any> select(entityType: KClass<T>): Selection<out DeferredResult<T>> =
        entityStore.select(entityType)

    @JvmDefault
    suspend fun <T : Any> select(entityType: KClass<T>, vararg attributes: QueryableAttribute<T, *>): Selection<out
    DeferredResult<T>> =
        entityStore.select(entityType, *attributes)

    @JvmDefault
    suspend fun select(vararg expressions: Expression<*>): Selection<out DeferredResult<Tuple>> =
        entityStore.select(*expressions)

    @JvmDefault
    suspend fun <T : Any, ID> findById(entityType: KClass<T>, id: ID): T? =
        entityStore.findByKey(entityType, id).await()

    @JvmDefault
    suspend infix fun <T : Any> findAll(entityType: KClass<T>): List<T> {
        return entityStore
            .select(entityType)
            .get()
            .toDeferred { it.toList() }
            .await()
    }

    @JvmDefault
    suspend infix fun <T : Any> refresh(entity: T): T =
        entityStore.refresh(entity).await()

    @JvmDefault
    suspend fun <T : Any> refresh(entity: T, vararg attributes: Attribute<*, *>): T =
        entityStore.refresh(entity, *attributes).await()

    @JvmDefault
    suspend fun <T : Any> refreshAll(entities: Iterable<T>, vararg attributes: Attribute<*, *>): List<T> =
        entityStore.refresh<T>(entities, *attributes).await()

    @JvmDefault
    suspend infix fun <T : Any> refreshAllProperties(entity: T): T =
        entityStore.refreshAll(entity).await()

    @JvmDefault
    suspend fun <T : Any> refreshAllEntities(entities: Iterable<T>, vararg attributes: Attribute<T, *>): List<T> {
        return entities.map { refreshAllProperties(it) }
    }

    @JvmDefault
    suspend infix fun <T : Any> upsert(entity: T): T =
        entityStore.upsert(entity).await()

    @JvmDefault
    suspend infix fun <T : Any> upsertAll(entities: Iterable<T>): List<T> {
        if(entities.none())
            return emptyList()

        return entityStore.upsert<T>(entities).await()
    }

    @JvmDefault
    suspend infix fun <T : Any> insert(entity: T): T =
        entityStore.insert(entity).await()

    suspend fun <T : Any, K : Any> insert(entity: T, keyKlass: KClass<K>): K =
        entityStore.insert(entity, keyKlass).await()

    @JvmDefault
    suspend infix fun <T : Any> insert(entityType: KClass<T>): Insertion<out DeferredResult<Tuple>> =
        entityStore.insert<T>(entityType)

    @JvmDefault
    suspend fun <T : Any> insert(entityType: KClass<T>,
                                 vararg attributes: QueryableAttribute<T, *>): InsertInto<out DeferredResult<Tuple>> =
        entityStore.insert(entityType, *attributes)

    @JvmDefault
    suspend infix fun <T : Any> insertAll(entities: Iterable<T>): List<T> =
        entityStore.insert<T>(entities).await()

    @JvmDefault
    suspend fun <T : Any, K : Any> insertAll(entities: Iterable<T>, keyKlass: KClass<K>): List<K> =
        entityStore.insert<K, T>(entities, keyKlass).await()

    @JvmDefault
    suspend fun <T : Any> update(): Update<out DeferredScalar<Int>> =
        entityStore.update()

    @JvmDefault
    suspend infix fun <T : Any> update(entity: T): T =
        entityStore.update(entity).await()

    @JvmDefault
    suspend infix fun <T : Any> update(entityType: KClass<T>): Update<out DeferredScalar<Int>> =
        entityStore.update<T>(entityType)

    @JvmDefault
    suspend infix fun <T : Any> updateAll(entities: Iterable<T>): List<T> =
        entityStore.update<T>(entities).await()

    @JvmDefault
    suspend fun delete(): Deletion<out DeferredScalar<Int>> =
        entityStore.delete()

    @JvmDefault
    suspend infix fun <T : Any> delete(entity: T) {
        entityStore.delete(entity).await()
    }

    @JvmDefault
    suspend infix fun <T : Any> delete(entityType: KClass<T>): Deletion<out DeferredScalar<Int>> =
        entityStore.delete<T>(entityType)

    @JvmDefault
    suspend infix fun <T : Any> deleteAll(entities: Iterable<T>) {
        entityStore.delete<T>(entities).await()
    }

    @JvmDefault
    suspend infix fun <T : Any> deleteAll(entityType: KClass<T>): Int =
        entityStore.delete<T>(entityType).get().call()

    @JvmDefault
    suspend infix fun <T : Any> count(entityType: KClass<T>): Selection<out DeferredScalar<Int>> =
        entityStore.count(entityType)

    @JvmDefault
    suspend fun <E : Any> count(vararg attributes: QueryableAttribute<Any, *>): Selection<out DeferredScalar<Int>> =
        entityStore.count(*attributes)


    @Suppress("UNCHECKED_CAST")
    @JvmDefault
    suspend fun <E : Any> count(entityType: KClass<E>, whereClause: QueryElement<out Result<E>>): Int {

        val query = select(Count.count(entityType.java))
            .applyWhereConditions(whereClause.whereElements)
            .unwrapQuery()

        val tuple = query.get().first()
        return tuple[0]
    }

    @JvmDefault
    suspend fun <E : Any> exists(entityType: KClass<E>, whereClause: QueryElement<out Result<E>>): Boolean =
        whereClause.limit(1).get().firstOrNull() != null

    @JvmDefault
    suspend fun raw(query: String, vararg parameters: Any): DeferredResult<Tuple> =
        entityStore.raw(query, *parameters)

    @JvmDefault
    suspend fun <T : Any> raw(entityType: KClass<T>, query: String, vararg parameters: Any): DeferredResult<T> =
        entityStore.raw(entityType, query, *parameters)

    @JvmDefault
    suspend fun <T : Any> withTransaction(block: CoroutineRequeryOperations.() -> T): T {
        return withTransaction(null, block)
    }

    suspend fun <T : Any> withTransaction(isolation: TransactionIsolation?,
                                          block: CoroutineRequeryOperations.() -> T): T

    @JvmDefault
    suspend fun <T : Any> withDataStore(block: KotlinEntityDataStore<Any>.() -> T): T {
        return block.invoke(entityStore.dataStore)
    }

}