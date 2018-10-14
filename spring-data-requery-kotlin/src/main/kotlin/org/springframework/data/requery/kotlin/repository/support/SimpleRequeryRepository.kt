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
import io.requery.query.function.Count
import mu.KotlinLogging
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
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.foldConditions
import org.springframework.data.requery.kotlin.getAsResultEntity
import org.springframework.data.requery.kotlin.getKeyExpression
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.reflect.KClass

/**
 *
 * [org.springframework.data.requery.kotlin.repository.RequeryRepository] 의 기본 구현체
 *
 * @author debop
 */
@Suppress("UNCHECKED_CAST")
@Repository
@Transactional(readOnly = true)
class SimpleRequeryRepository<E : Any, ID : Any> @Autowired constructor(
    final val entityInformation: RequeryEntityInformation<E, ID>,
    override val operations: RequeryOperations
) : RequeryRepositoryImplementation<E, ID> {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    final override val domainKlass: KClass<E> = entityInformation.kotlinType
    final override val domainClass: Class<E> = entityInformation.javaType
    final val domainClassName: String = domainClass.simpleName ?: "Unknown"

    private var crudMethodMetadata: CrudMethodMetadata? = null

    init {
        log.info { "Create SimpleRequeryRepository for domainClass=${domainKlass.simpleName}" }
    }

    override fun setRepositoryMethodMetadata(crudMethodMetadata: CrudMethodMetadata?) {
        this.crudMethodMetadata = crudMethodMetadata
    }

    fun select(): QueryElement<out Result<E>> = operations.select(domainKlass).unwrap()

    override fun findAll(): List<E> = operations.findAll(domainKlass)

    override fun findAll(sort: Sort): List<E> {
        return select()
            .applySort(domainClass, sort)
            .get()
            .toList()
    }

    override fun <S : E> findAll(example: Example<S>): List<S> {
        return example.buildQueryElement(operations, domainKlass as KClass<S>).get().toList()
    }

    override fun <S : E> findAll(example: Example<S>, sort: Sort): List<S> {
        return example
            .buildQueryElement(operations, domainKlass)
            .applySort(domainClass, sort)
            .get()
            .toList()
    }

    override fun findAll(pageable: Pageable): Page<E> {
        log.trace { "Fild all $domainClassName with paging. pageable=$pageable" }

        return when {
            pageable.isPaged -> {
                val content = select().applyPageable(domainClass, pageable).getAsResultEntity<E>().toList()
                val totals = operations.count(domainKlass).get().value().toLong()

                PageImpl(content, pageable, totals)
            }
            else -> {
                val content = select().get().toList()
                PageImpl(content)
            }
        }
    }

    override fun <S : E> findAll(example: Example<S>, pageable: Pageable): Page<S> {

        log.trace { "Find all [$domainClass] with paging. pageable=$pageable" }

        val queryElement = example.buildQueryElement(operations, domainKlass).unwrap()

        return when {
            pageable.isPaged -> {
                val totals = count(example)
                val contents = queryElement.applyPageable(domainClass, pageable).get().toList()

                PageImpl<S>(contents, pageable, totals)
            }
            else -> {
                val contents = queryElement.get().toList()
                PageImpl<S>(contents)
            }
        }
    }

    override fun findAll(filter: Return<out Result<E>>): List<E> {
        return filter.get().toList()
    }

    override fun findAll(filter: QueryElement<out Result<E>>, pageable: Pageable): Page<E> {
        return when {
            pageable.isPaged -> {
                val totals = count(filter)
                val contents = filter.applyPageable(domainClass, pageable).get().toList()
                PageImpl<E>(contents, pageable, totals)
            }
            else -> PageImpl<E>(findAll(filter))
        }
    }

    override fun findAll(conditions: Iterable<Condition<E, *>>): List<E> {
        val whereClause = conditions.foldConditions()

        return whereClause?.let {
            select().where(it).get().toList()
        } ?: emptyList()
    }

    override fun findAll(conditions: Iterable<Condition<E, *>>, pageable: Pageable): Page<E> {
        return when {
            pageable.isPaged -> {
                val whereClause = conditions.foldConditions()
                val baseQuery = whereClause?.let {
                    select().where(it).unwrap()
                } ?: select()

                val contents = baseQuery.unwrap().applyPageable(domainClass, pageable).get().toList()
                PageImpl<E>(contents, pageable, count(baseQuery))
            }
            else ->
                PageImpl<E>(findAll(conditions))
        }
    }

    override fun findAll(conditions: Iterable<Condition<E, *>>, sort: Sort): List<E> {
        return select()
            .where(conditions.foldConditions())
            .applySort(domainClass, sort)
            .get()
            .toList()
    }

    override fun findAllById(ids: Iterable<ID>): List<E> {

        log.trace { "Find all by id. ids=$ids" }

        val keyExpr = domainClass.getKeyExpression<ID>()

        return operations
            .select(domainKlass)
            .where(keyExpr.`in`(ids.toSet()))
            .get()
            .toList()
    }

    override fun <S : E> saveAll(entities: Iterable<S>): List<S> {
        return operations.upsertAll(entities)
    }

    override fun insert(entity: E): E = operations.insert(entity)

    override fun <K : Any> insert(entity: E, keyClass: KClass<K>): K {
        return operations.insert(entity, keyClass).also {
            log.trace { "Insert entity, new key=$it" }
        }
    }

    override fun insertAll(entities: Iterable<E>): List<E> {
        return operations.insertAll(entities)
    }

    override fun <K : Any> insertAll(entities: Iterable<E>, keyClass: KClass<K>): List<K> {
        return operations.insertAll(entities, keyClass).also {
            log.trace { "Insert entities, new keys=$it" }
        }
    }

    override fun upsert(entity: E): E {
        return operations.upsert(entity)
    }

    override fun upsertAll(entities: Iterable<E>): List<E> {
        return operations.upsertAll(entities)
    }

    override fun refresh(entity: E): E {
        return operations.refresh(entity)
    }

    override fun refreshAllProperties(entity: E): E {
        return operations.refreshAllProperties(entity)
    }

    override fun refreshAll(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> {
        return operations.refreshAll(entities, *attributes)
    }

    override fun refreshAllEntities(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> {
        return operations.refreshAllEntities(entities, *attributes)
    }

    override fun deleteInBatch(entities: Iterable<E>) {
        operations.deleteAll(entities)
    }

    override fun deleteAllInBatch(): Int {
        return operations.deleteAll(domainKlass)
    }

    override fun getOne(id: ID): E? {
        return operations.findById(domainKlass, id)
    }

    override fun <S : E> save(entity: S): S {
        return operations.upsert(entity)
    }

    override fun deleteById(id: ID) {
        log.trace { "Delete $domainClassName by id [$id]" }

        val keyExpr = domainClass.getKeyExpression<ID>()

        val deletedCount = operations
            .delete(domainKlass)
            .where(keyExpr.eq(id))
            .get()
            .value()

        log.trace { "Delete $domainClassName by id [$id]. deleted count=$deletedCount" }
    }

    override fun deleteAll(entities: MutableIterable<E>) {
        operations.deleteAll(entities)
    }

    override fun deleteAll() {
        log.debug { "Delete all entities ... domainClass=$domainClassName" }
        operations.deleteAll(domainKlass)
    }

    override fun count(): Long {
        return operations.count(domainKlass).get().value().toLong()
    }

    override fun <S : E> count(example: Example<S>): Long {

        return count(example.buildQueryElement(operations, domainKlass as KClass<S>) as QueryElement<out Result<E>>)
    }

    override fun count(queryElement: QueryElement<out Result<E>>): Long {
        return operations.count(domainKlass, queryElement).toLong()
    }

    override fun existsById(id: ID): Boolean {
        val keyExpr = domainClass.getKeyExpression<ID>()

        val tuple = operations
            .select(Count.count(domainKlass.java))
            .where(keyExpr.eq(id))
            .get()
            .firstOrNull()

        return tuple.get<Int>(0) > 0
    }

    override fun findById(id: ID): Optional<E> {
        return Optional.ofNullable(operations.findById(domainKlass, id))
    }

    override fun delete(entity: E) {
        log.trace { "Delete entity. entity=$entity" }
        operations.delete(entity)
    }

    override fun <S : E> findOne(example: Example<S>): Optional<S> {
        val entity = example.buildQueryElement(operations, domainKlass)
            .limit(1)
            .get()
            .firstOrNull()

        return Optional.ofNullable(entity)
    }

    override fun findOne(filter: Return<out Result<E>>): Optional<E> {
        val count = count(filter.unwrap()).toInt()
        if(count > 1) {
            throw IncorrectResultSizeDataAccessException(1, count)
        }
        return Optional.ofNullable(filter.get().firstOrNull())
    }

    override fun <S : E> exists(example: Example<S>): Boolean {
        return example
            .buildQueryElement(operations, domainKlass)
            .limit(1)
            .get()
            .firstOrNull() != null
    }

    override fun exists(queryElement: QueryElement<out Result<E>>): Boolean {
        return operations.exists(domainKlass, queryElement)
    }

}