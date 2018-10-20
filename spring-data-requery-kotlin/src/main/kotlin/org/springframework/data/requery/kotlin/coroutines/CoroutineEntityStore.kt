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
import io.requery.kotlin.BlockingEntityStore
import io.requery.kotlin.Deletion
import io.requery.kotlin.EntityStore
import io.requery.kotlin.InsertInto
import io.requery.kotlin.Insertion
import io.requery.kotlin.QueryDelegate
import io.requery.kotlin.QueryableAttribute
import io.requery.kotlin.Selection
import io.requery.kotlin.Update
import io.requery.meta.Attribute
import io.requery.query.Expression
import io.requery.query.Result
import io.requery.query.Return
import io.requery.query.Scalar
import io.requery.query.Tuple
import io.requery.sql.KotlinEntityDataStore
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.async
import mu.KLogging
import kotlin.reflect.KClass

/**
 * Kotlin Coroutine을 이용하여 [io.requery.sql.EntityDataStore]를 구현한 클래스입니다.
 * 이 클래스는 Requery의 [io.requery.async.CompletableEntityStore]를 대체할 수 있는 기능을 제공합니다.
 *
 * 비동기 방식이지만, Coroutine은 Lightweight thread이므로 Transaction에 안정하게 구현됩니다.
 * 다만 Dispatchers.Default를 쓰면 안되고, caller thread에 속해서 작동해야 하므로 [Dispatchers.Unconfined]를 사용해야 합니다.
 *
 * @author debop
 * @since 18. 5. 16
 */
class CoroutineEntityStore<T : Any>(val dataStore: KotlinEntityDataStore<T>) : EntityStore<T, Any> {

    companion object : KLogging()

    override fun close() = dataStore.close()

    override infix fun <E : T> select(type: KClass<E>): Selection<out DeferredResult<E>> {
        return resultAsync(dataStore.select(type))
    }

    override fun <E : T> select(type: KClass<E>, vararg attributes: QueryableAttribute<E, *>): Selection<out DeferredResult<E>> {
        return resultAsync(dataStore.select(type, *attributes))
    }

    override fun select(vararg expressions: Expression<*>): Selection<out DeferredResult<Tuple>> {
        return resultAsync(dataStore.select(*expressions))
    }

    override fun <E : T> insert(type: KClass<E>): Insertion<out DeferredResult<Tuple>> {
        return resultAsync(dataStore.insert(type))
    }

    override fun <E : T> insert(type: KClass<E>,
                                vararg attributes: QueryableAttribute<E, *>): InsertInto<out DeferredResult<Tuple>> {
        return resultAsync(dataStore.insert(type, *attributes))
    }

    override fun update(): Update<out DeferredScalar<Int>> {
        return scalarAsync(dataStore.update())
    }

    override fun <E : T> update(type: KClass<E>): Update<out DeferredScalar<Int>> {
        return scalarAsync(dataStore.update(type))
    }

    override fun delete(): Deletion<out DeferredScalar<Int>> {
        return scalarAsync(dataStore.delete())
    }

    override fun <E : T> delete(type: KClass<E>): Deletion<out DeferredScalar<Int>> {
        return scalarAsync(dataStore.delete(type))
    }

    override fun <E : T> count(type: KClass<E>): Selection<out DeferredScalar<Int>> {
        return scalarAsync(dataStore.count(type))
    }

    override fun count(vararg attributes: QueryableAttribute<T, *>): Selection<out DeferredScalar<Int>> {
        return scalarAsync(dataStore.count(*attributes))
    }

    override fun <E : T> insert(entity: E): Deferred<E> {
        return execute { dataStore.insert(entity) }
    }

    override fun <E : T> insert(entities: Iterable<E>): Deferred<List<E>> {
        return execute { dataStore.insert(entities).toList() }
    }

    override fun <K : Any, E : T> insert(entity: E, keyClass: KClass<K>): Deferred<K> {
        return execute { dataStore.insert(entity, keyClass) }
    }

    override fun <K : Any, E : T> insert(entities: Iterable<E>, keyClass: KClass<K>): Deferred<List<K>> {
        return execute { dataStore.insert(entities, keyClass).toList() }
    }

    override fun <E : T> update(entity: E): Deferred<E> {
        return execute { dataStore.update(entity) }
    }

    override fun <E : T> update(entities: Iterable<E>): Deferred<List<E>> {
        return execute { dataStore.update(entities).toList() }
    }

    override fun <E : T> upsert(entity: E): Deferred<E> {
        return execute { dataStore.upsert(entity) }
    }

    override fun <E : T> upsert(entities: Iterable<E>): Deferred<List<E>> {
        return execute { dataStore.upsert(entities).toList() }
    }

    override fun <E : T> refresh(entity: E): Deferred<E> {
        return execute { dataStore.refresh(entity) }
    }

    override fun <E : T> refresh(entity: E, vararg attributes: Attribute<*, *>): Deferred<E> {
        return execute { dataStore.refresh(entity, *attributes) }
    }

    override fun <E : T> refresh(entities: Iterable<E>, vararg attributes: Attribute<*, *>): Deferred<List<E>> {
        return execute { dataStore.refresh(entities, *attributes).toList() }
    }

    override fun <E : T> refreshAll(entity: E): Deferred<E> {
        return execute { dataStore.refreshAll(entity) }
    }

    override fun <E : T> delete(entity: E): Deferred<*> {
        return execute { dataStore.delete(entity) }
    }

    override fun <E : T> delete(entities: Iterable<E>): Deferred<*> = execute { dataStore.delete(entities) }


    override fun raw(query: String, vararg parameters: Any): DeferredResult<Tuple> {
        return DeferredResult(dataStore.raw(query, parameters))
    }

    override fun <E : T> raw(type: KClass<E>, query: String, vararg parameters: Any): DeferredResult<E> {
        return DeferredResult(dataStore.raw(type, query, *parameters))
    }

    override fun <E : T, K> findByKey(type: KClass<E>, key: K): Deferred<E?> =
        execute { dataStore.findByKey(type, key) }

    override fun toBlocking(): BlockingEntityStore<T> = dataStore

    fun <V> withTransaction(body: BlockingEntityStore<T>.() -> V): Deferred<V> =
        execute { dataStore.withTransaction(body) }

    fun <V> withTransaction(isolation: TransactionIsolation = TransactionIsolation.SERIALIZABLE,
                            body: BlockingEntityStore<T>.() -> V): Deferred<V> =
        execute { dataStore.withTransaction(isolation, body) }

    /**
     * [CoroutineEntityStore]에서 제공하는 모든 메소드는 이 함수를 통해서 실행됩니다.
     *
     * @param V return value type
     * @param block code block of requery operations
     */
    inline fun <V> execute(crossinline block: suspend CoroutineEntityStore<T>.() -> V): Deferred<V> {
        return RequeryScope.async {
            block.invoke(this@CoroutineEntityStore)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> resultAsync(query: Return<out Result<E>>): QueryDelegate<out DeferredResult<E>> {
        val element = query as QueryDelegate<Result<E>>
        return element.extend(io.requery.util.function.Function { DeferredResult(it) })
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> scalarAsync(query: Return<out Scalar<E>>): QueryDelegate<out DeferredScalar<E>> {
        val element = query as QueryDelegate<Scalar<E>>
        return element.extend(io.requery.util.function.Function { DeferredScalar(it) })
    }
}