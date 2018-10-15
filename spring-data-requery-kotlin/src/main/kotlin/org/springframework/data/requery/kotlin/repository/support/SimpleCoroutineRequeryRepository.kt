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

package org.springframework.data.requery.kotlin.repository.support

import io.requery.meta.Attribute
import io.requery.query.Condition
import io.requery.query.Result
import io.requery.query.Return
import io.requery.query.element.QueryElement
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.requery.kotlin.applyPageable
import org.springframework.data.requery.kotlin.applySort
import org.springframework.data.requery.kotlin.buildQueryElement
import org.springframework.data.requery.kotlin.coroutines.CoroutineEntityStore
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.foldConditions
import org.springframework.data.requery.kotlin.getAsResultEntity
import org.springframework.data.requery.kotlin.getKeyExpression
import org.springframework.data.requery.kotlin.repository.CoroutineRequeryRepository
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

/**
 * [CoroutineRequeryRepository]의 기본 구현체
 *
 * @author debop
 * @since 18. 10. 14
 */
@Repository
@Transactional
class SimpleCoroutineRequeryRepository<E : Any, ID : Any> @Autowired constructor(
    val entityInformation: RequeryEntityInformation<E, ID>,
    override val operations: CoroutineRequeryOperations
) : CoroutineRequeryRepositoryImplementation<E, ID> {

    companion object : KLogging()

    init {
        logger.info { "Create ${javaClass.simpleName} for domain class `$domainClassName`" }
    }

    final override val domainKlass: KClass<E> get() = entityInformation.kotlinType
    final val domainClassName: String = domainKlass.simpleName ?: "Unknown"

    val coroutineEntityStore: CoroutineEntityStore<Any> by lazy { CoroutineEntityStore(operations.dataStore) }

    private var crudMethodMetadata: CrudMethodMetadata? = null

    override fun setRepositoryMethodMetadata(crudMethodMetadata: CrudMethodMetadata?) {
        this.crudMethodMetadata = crudMethodMetadata
    }

    suspend fun select(): QueryElement<out Result<E>> = operations.select(domainKlass).unwrap()

    private inline fun <T : Any> async(crossinline block: suspend () -> T): Deferred<T> {
        return GlobalScope.async(coroutineDispatcher) {
            block.invoke()
        }
    }

    override suspend fun findAll(): List<E> {
        return operations.findAll(domainKlass)
    }

    override suspend fun findAll(sort: Sort): List<E> {
        return if(sort.isSorted) {
            select()
                .applySort(domainKlass, sort)
                .get()
                .toList()
        } else {
            findAll()
        }
    }

    override suspend fun findAll(pageable: Pageable): Page<E> {
        return if(pageable.isPaged) {
            val content = async {
                select().applyPageable(domainKlass, pageable).getAsResultEntity<E>().toList()
            }
            val totals = async { operations.count(domainKlass).get().value().toLong() }

            PageImpl(content.await(), pageable, totals.await())
        } else {
            val content = async { select().get().toList() }
            PageImpl(content.await())
        }
    }

    override suspend fun <S : E> findAll(example: Example<S>, sort: Sort): List<S> {

        val queryElement = example
            .buildQueryElement(operations, domainKlass)
            .applySort(domainKlass, sort)

        return coroutineEntityStore.execute {
            queryElement.get().toList()
        }.await()
    }

    override suspend fun <S : E> findAll(example: Example<S>, pageable: Pageable): Page<S> {
        val queryElement = example
            .buildQueryElement(operations, domainKlass)
            .unwrapQuery()

        return if(pageable.isPaged) {
            val contents = async { queryElement.get().toList() }
            val totals = async { count(example) }
            PageImpl<S>(contents.await(), pageable, totals.await())
        } else {
            val contents = async { queryElement.get().toList() }
            PageImpl<S>(contents.await())
        }
    }

    override suspend fun findAll(filter: Return<out Result<E>>): List<E> {
        return filter.get().toList()
    }

    override suspend fun findAll(filter: Return<out Result<E>>, sort: Sort): List<E> {
        return filter.applySort(domainKlass, sort).get().toList()
    }

    override suspend fun findAll(filter: Return<out Result<E>>, pageable: Pageable): Page<E> {
        return if(pageable.isPaged) {
            val contents = async { filter.applyPageable(domainKlass, pageable).get().toList() }
            val totals = async { count(filter.unwrap()) }
            PageImpl<E>(contents.await(), pageable, totals.await())
        } else {
            PageImpl<E>(findAll(filter))
        }
    }

    override suspend fun findAll(conditions: Iterable<Condition<E, *>>): List<E> {
        val whereClause = conditions.foldConditions()
        return whereClause?.let {
            async { select().where(it).get().toList() }.await()
        } ?: emptyList()
    }

    override suspend fun findAll(conditions: Iterable<Condition<E, *>>, sort: Sort): List<E> {
        val whereClause = conditions.foldConditions()
        val baseQuery = whereClause?.let { select().where(it).unwrap() } ?: select()

        return baseQuery.applySort(domainKlass, sort).get().toList()
    }

    override suspend fun findAll(conditions: Iterable<Condition<E, *>>, pageable: Pageable): Page<E> {
        return if(pageable.isPaged) {
            val whereClause = conditions.foldConditions()
            val baseQuery = whereClause?.let { select().where(it).unwrap() } ?: select()

            val contents = async { baseQuery.applyPageable(domainKlass, pageable).get().toList() }
            val totals = async { count(baseQuery) }

            PageImpl<E>(contents.await(), pageable, totals.await())
        } else {
            PageImpl<E>(findAll(conditions))
        }
    }

    override suspend fun <S : E> saveAll(entities: Iterable<S>): List<S> {
        return operations.upsertAll(entities)
    }

    override suspend fun <K : Any> insert(entity: E, keyKlass: KClass<K>): K {
        return operations.insert(entity, keyKlass)
    }

    override suspend fun insertAll(entities: Iterable<E>): List<E> {
        return operations.insertAll(entities)
    }

    override suspend fun <K : Any> insertAll(entities: Iterable<E>, keyKlass: KClass<K>): List<K> {
        return operations.insertAll(entities, keyKlass)
    }

    override suspend fun upsert(entity: E): E =
        operations.upsert(entity)

    override suspend fun upsertAll(entities: Iterable<E>): List<E> {
        if(entities.none())
            return emptyList()

        return operations.upsertAll(entities)
    }

    override suspend fun refresh(entity: E): E = operations.refresh(entity)

    override suspend fun refreshAllProperties(entity: E): E =
        operations.refreshAllProperties(entity)

    override suspend fun refreshAll(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> =
        operations.refreshAll(entities, *attributes)

    override suspend fun refreshAllEntities(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> =
        operations.refreshAllEntities(entities, *attributes)

    override suspend fun deleteInBatch(entities: Iterable<E>) {
        if(!entities.none()) {
            operations.deleteAll(entities)
        }
    }

    override suspend fun deleteAllInBatch(): Int =
        operations.deleteAll(domainKlass)

    override suspend fun getOne(id: ID): E? =
        operations.findById(domainKlass, id)

    override suspend fun <S : E> save(entity: S): S =
        operations.upsert(entity)

    override suspend fun findById(id: ID): E? =
        operations.findById(domainKlass, id)

    override suspend fun existsById(id: ID): Boolean =
        findById(id) != null

    override suspend fun findAllById(ids: Iterable<ID>): List<E> {
        val keyExpr = domainKlass.getKeyExpression<ID>()

        return select()
            .where(keyExpr.`in`(ids.toSet()))
            .get()
            .toList()
    }

    override suspend fun count(): Long =
        operations.count(domainKlass).get().value().toLong()

    @Suppress("UNCHECKED_CAST")
    override suspend fun <S : E> count(example: Example<S>): Long {
        return count(example.buildQueryElement(operations, domainKlass))
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <S : E> count(queryElement: QueryElement<out Result<S>>): Long =
        operations.count(domainKlass as KClass<S>, queryElement).toLong()

    override suspend fun deleteById(id: ID) {
        val keyExpr = domainKlass.getKeyExpression<ID>()

        val deletedCount = operations
            .delete(domainKlass)
            .where(keyExpr.eq(id))
            .get()
            .value()
        logger.trace { "Delete $domainClassName by id=[$id]. deleted count=$deletedCount" }
    }

    override suspend fun delete(entity: E) {
        operations.delete(entity)
    }

    override suspend fun deleteAll(entities: Iterable<E>) {
        if(!entities.none()) {
            operations.deleteAll(entities)
        }
    }

    override suspend fun deleteAll() {
        operations.deleteAll(domainKlass)
    }

    override suspend fun <S : E> findOne(example: Example<S>): S? {
        return findOne(example.buildQueryElement(operations, domainKlass))
    }

    override suspend fun <S : E> findOne(filter: Return<out Result<S>>): S? {
        val count = count(filter.unwrap()).toInt()
        if(count > 1) {
            throw IncorrectResultSizeDataAccessException(1, count)
        }
        return filter.get().firstOrNull()
    }

    override suspend fun <S : E> exists(example: Example<S>): Boolean {
        return exists(example.buildQueryElement(operations, domainKlass))
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <S : E> exists(queryElement: QueryElement<out Result<S>>): Boolean =
        operations.exists(domainKlass as KClass<S>, queryElement)
}