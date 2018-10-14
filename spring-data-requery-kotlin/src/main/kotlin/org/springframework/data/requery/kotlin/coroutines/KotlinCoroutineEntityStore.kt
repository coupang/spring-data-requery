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
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import mu.KotlinLogging
import kotlin.reflect.KClass

/**
 * Kotlin Coroutine을 이용하여 [io.requery.sql.EntityDataStore]를 구현한 클래스입니다.
 * TODO: 이 클래스를 단독으로 쓸 것인지, [KotlinCoroutineRequeryOperations] 처럼 suspend 만 붙일 것인지 결정해야 한다
 *
 * 비동기 방식이지만, Coroutine은 Lightweight thread이므로 Transaction에 안정하게 구현됩니다.
 * 다만 Dispatchers.Default를 쓰면 안되고, caller thread에 속해서 작동해야 하므로 [Dispatchers.Unconfined]를 사용해야 합니다.
 *
 * @author debop
 * @since 18. 5. 16
 */
class KotlinCoroutineEntityStore<T : Any>(private val dataStore: BlockingEntityStore<T>,
                                          val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Unconfined) : EntityStore<T, Any> {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    override fun close() = dataStore.close()

    override infix fun <E : T> select(type: KClass<E>): Selection<out DeferredResult<E>> {
        log.trace { "select entities. type=$type" }
        return result(dataStore.select(type))
    }

    override fun <E : T> select(type: KClass<E>, vararg attributes: QueryableAttribute<E, *>): Selection<out DeferredResult<E>> {
        return result(dataStore.select(type, *attributes))
    }

    override fun select(vararg expressions: Expression<*>): Selection<out DeferredResult<Tuple>> {
        return result(dataStore.select(*expressions))
    }

    override fun <E : T> insert(type: KClass<E>): Insertion<out DeferredResult<Tuple>> {
        return result(dataStore.insert(type))
    }

    override fun <E : T> insert(type: KClass<E>, vararg attributes: QueryableAttribute<E, *>): InsertInto<out DeferredResult<Tuple>> {
        return result(dataStore.insert(type, *attributes))
    }

    override fun update(): Update<DeferredScalar<Int>> {
        return scalar(dataStore.update())
    }

    override fun <E : T> update(type: KClass<E>): Update<DeferredScalar<Int>> {
        return scalar(dataStore.update(type))
    }

    override fun delete(): Deletion<DeferredScalar<Int>> {
        return scalar(dataStore.delete())
    }

    override fun <E : T> delete(type: KClass<E>): Deletion<DeferredScalar<Int>> {
        return scalar(dataStore.delete(type))
    }

    override fun <E : T> count(type: KClass<E>): Selection<DeferredScalar<Int>> {
        log.trace { "get row count. type=$type" }
        return scalar(dataStore.count(type))
    }

    override fun count(vararg attributes: QueryableAttribute<T, *>): Selection<DeferredScalar<Int>> {
        return scalar(dataStore.count(*attributes))
    }

    override fun <E : T> insert(entity: E): Deferred<E> {
        log.trace { "insert entity. $entity" }
        return execute { dataStore.insert(entity) }
    }

    override fun <E : T> insert(entities: Iterable<E>): Deferred<Iterable<E>> {
        return execute { dataStore.insert(entities) }
    }

    override fun <K : Any, E : T> insert(entity: E, keyClass: KClass<K>): Deferred<K> {
        log.trace { "insert entity. $entity, keyClass=$keyClass" }
        return execute { dataStore.insert(entity, keyClass) }
    }

    override fun <K : Any, E : T> insert(entities: Iterable<E>, keyClass: KClass<K>): Deferred<Iterable<K>> {
        return execute { dataStore.insert(entities, keyClass) }
    }

    override fun <E : T> update(entity: E): Deferred<E> {
        log.trace { "update entity. $entity" }
        return execute { dataStore.update(entity) }
    }

    override fun <E : T> update(entities: Iterable<E>): Deferred<Iterable<E>> {
        return execute { dataStore.update(entities) }
    }

    override fun <E : T> upsert(entity: E): Deferred<E> {
        log.trace { "upsert entity. $entity" }
        return execute { dataStore.upsert(entity) }
    }

    override fun <E : T> upsert(entities: Iterable<E>): Deferred<Iterable<E>> {
        return execute { dataStore.upsert(entities) }
    }

    override fun <E : T> refresh(entity: E): Deferred<E> {
        log.trace { "refresh entity. $entity" }
        return execute { dataStore.refresh(entity) }
    }

    override fun <E : T> refresh(entity: E, vararg attributes: Attribute<*, *>): Deferred<E> {
        log.trace { "refresh entity. $entity, attributes=$attributes" }
        return execute { dataStore.refresh(entity, *attributes) }
    }

    override fun <E : T> refresh(entities: Iterable<E>, vararg attributes: Attribute<*, *>): Deferred<Iterable<E>> {
        return execute { dataStore.refresh(entities, *attributes) }
    }

    override fun <E : T> refreshAll(entity: E): Deferred<E> {
        log.trace { "refreshAll entity. $entity" }
        return execute { dataStore.refreshAll(entity) }
    }

    override fun <E : T> delete(entity: E): Deferred<*> {
        log.trace { "delete entity. $entity" }
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
     * [KotlinCoroutineEntityStore]에서 제공하는 모든 메소드는 이 함수를 통해서 실행됩니다.
     *
     * @param V return value type
     * @param block code block of requery operations
     */
    inline fun <V> execute(crossinline block: KotlinCoroutineEntityStore<T>.() -> V): Deferred<V> {
        return GlobalScope.async(coroutineDispatcher) {
            block.invoke(this@KotlinCoroutineEntityStore)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> result(query: Return<out Result<E>>): QueryDelegate<DeferredResult<E>> {
        val element = query as QueryDelegate<Result<E>>
        return element.extend(io.requery.util.function.Function { result -> DeferredResult(result, coroutineDispatcher) })
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E> scalar(query: Return<out Scalar<E>>): QueryDelegate<DeferredScalar<E>> {
        val element = query as QueryDelegate<Scalar<E>>
        return element.extend(io.requery.util.function.Function { result -> DeferredScalar(result, coroutineDispatcher) })
    }
}