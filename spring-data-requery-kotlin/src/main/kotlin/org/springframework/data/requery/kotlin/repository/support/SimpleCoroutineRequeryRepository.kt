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
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.requery.kotlin.coroutines.KotlinCoroutineRequeryOperations
import org.springframework.data.requery.kotlin.repository.CoroutineRequeryRepository
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
    override val operations: KotlinCoroutineRequeryOperations
) : CoroutineRequeryRepositoryImplementation<E, ID> {

    companion object : KLogging()

    init {
        logger.info { "Create ${javaClass.simpleName} for domain class `$domainClassName`" }
    }

    final override val domainKlass: KClass<E> get() = entityInformation.kotlinType
    final val domainClassName: String = domainKlass.simpleName ?: "Unknown"

    override fun setRepositoryMethodMetadata(crudMethodMetadata: CrudMethodMetadata?) {
        TODO("not implemented")
    }


    override suspend fun findAll(): List<E> {
        TODO("not implemented")
    }

    override suspend fun findAll(sort: Sort): MutableIterable<E> {
        TODO("not implemented")
    }

    override suspend fun findAll(pageable: Pageable): Page<E> {
        TODO("not implemented")
    }

    override suspend fun <S : E> findAll(example: Example<S>, sort: Sort): List<S> {
        TODO("not implemented")
    }

    override suspend fun <S : E> findAll(example: Example<S>, pageable: Pageable): Page<S> {
        TODO("not implemented")
    }

    override suspend fun findAll(filter: Return<out Result<E>>, sort: Sort): List<E> {
        TODO("not implemented")
    }

    override suspend fun findAll(filter: Return<out Result<E>>, pageable: Pageable): Page<E> {
        TODO("not implemented")
    }

    override suspend fun findAll(conditions: Iterable<Condition<E, *>>, sort: Sort): List<E> {
        TODO("not implemented")
    }

    override suspend fun findAll(conditions: Iterable<Condition<E, *>>, pageable: Pageable): Page<E> {
        TODO("not implemented")
    }

    override suspend fun <S : E> saveAll(entities: Iterable<S>): List<S> {
        TODO("not implemented")
    }

    override suspend fun <K : Any> insert(entity: E, keyClass: KClass<K>): K {
        TODO("not implemented")
    }

    override suspend fun insertAll(entities: Iterable<E>): List<E> {
        TODO("not implemented")
    }

    override suspend fun <K : Any> insertAll(entities: Iterable<E>, keyClass: KClass<K>): List<K> {
        TODO("not implemented")
    }

    override suspend fun upsert(entity: E): E {
        TODO("not implemented")
    }

    override suspend fun upsertAll(entities: Iterable<E>): List<E> {
        TODO("not implemented")
    }

    override suspend fun refresh(entity: E): E {
        TODO("not implemented")
    }

    override suspend fun refreshAllProperties(entity: E): E {
        TODO("not implemented")
    }

    override suspend fun refreshAll(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> {
        TODO("not implemented")
    }

    override suspend fun refreshAllEntities(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E> {
        TODO("not implemented")
    }

    override suspend fun deleteInBatch(entities: Iterable<E>) {
        TODO("not implemented")
    }

    override suspend fun deleteAllInBatch(): Int {
        TODO("not implemented")
    }

    override suspend fun getOne(id: ID): E? {
        TODO("not implemented")
    }

    override suspend fun <S : E> save(entity: S): S {
        TODO("not implemented")
    }

    override suspend fun findById(id: ID): E? {
        TODO("not implemented")
    }

    override suspend fun existsById(id: ID): Boolean {
        TODO("not implemented")
    }

    override suspend fun findAllById(ids: Iterable<ID>): Iterable<E> {
        TODO("not implemented")
    }

    override suspend fun count(): Long {
        TODO("not implemented")
    }

    override suspend fun <S : E> count(example: Example<S>): Long {
        TODO("not implemented")
    }

    override suspend fun count(queryElement: QueryElement<out Result<E>>): Long {
        TODO("not implemented")
    }

    override suspend fun deleteById(id: ID) {
        TODO("not implemented")
    }

    override suspend fun delete(entity: E) {
        TODO("not implemented")
    }

    override suspend fun deleteAll(entities: Iterable<E>) {
        TODO("not implemented")
    }

    override suspend fun deleteAll() {
        TODO("not implemented")
    }

    override suspend fun <S : E> findOne(example: Example<S>): S? {
        TODO("not implemented")
    }

    override suspend fun findOne(filter: Return<out Result<E>>): E? {
        TODO("not implemented")
    }

    override suspend fun <S : E> exists(example: Example<S>): Boolean {
        TODO("not implemented")
    }

    override suspend fun exists(queryElement: QueryElement<out Result<E>>): Boolean {
        TODO("not implemented")
    }
}