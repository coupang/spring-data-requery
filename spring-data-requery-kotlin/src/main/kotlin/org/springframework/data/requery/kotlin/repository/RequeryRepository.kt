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

package org.springframework.data.requery.kotlin.repository

import io.requery.meta.Attribute
import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.QueryByExampleExecutor
import org.springframework.data.requery.kotlin.core.RequeryOperations
import kotlin.reflect.KClass

/**
 * Requery를 사용하는 Spring Data Repository의 기본 Interface
 *
 * @author debop
 */
@NoRepositoryBean
interface RequeryRepository<E : Any, ID : Any> : PagingAndSortingRepository<E, ID>,
                                                 QueryByExampleExecutor<E>,
                                                 RequeryConditionExecutor<E> {
    val operations: RequeryOperations
    val domainKlass: KClass<E>
    val domainClass: Class<E>

    @Override
    override fun findAll(): List<E>

    @Override
    override fun findAll(sort: Sort): List<E>

    override fun findAllById(ids: Iterable<ID>): List<E>

    @Override
    override fun <S : E> saveAll(entities: Iterable<S>): List<S>

    fun insert(entity: E): E

    /**
     * Entity 추가 후, 발급된 Key 값을 반환합니다.
     * @param entity
     */
    fun <K : Any> insert(entity: E, keyClass: KClass<K>): K

    fun insertAll(entities: Iterable<E>): List<E>

    fun <K : Any> insertAll(entities: Iterable<E>, keyClass: KClass<K>): List<K>

    fun upsert(entity: E): E

    fun upsertAll(entities: Iterable<E>): List<E>

    fun refresh(entity: E): E

    fun refreshAllProperties(entity: E): E

    fun refreshAll(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E>

    fun refreshAllEntities(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E>

    fun deleteInBatch(entities: Iterable<E>)

    fun deleteAllInBatch(): Int

    fun getOne(id: ID): E?

    @Override
    override fun <S : E> findAll(example: Example<S>): List<S>

    @Override
    override fun <S : E> findAll(example: Example<S>, sort: Sort): List<S>
}