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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.repository.query.CoroutineQueryByExampleExecutor
import kotlin.reflect.KClass

/**
 * Requery를 Coroutine 환경에서 실행하는 Spring Data Repository의 기본 Interface
 *
 * @author debop
 * @since 18. 10. 14
 */
@NoRepositoryBean
interface CoroutineRequeryRepository<E : Any, ID : Any> : CoroutinePagingAndSortingRepository<E, ID>,
                                                          CoroutineQueryByExampleExecutor<E>,
                                                          CoroutineRequeryConditionExecutor<E> {

    val operations: CoroutineRequeryOperations
    val domainKlass: KClass<E>

    override suspend fun findAll(): List<E>

    override suspend fun findAll(sort: Sort): List<E>

    override suspend fun findAll(pageable: Pageable): Page<E>

    override suspend fun <S : E> saveAll(entities: Iterable<S>): List<S>

    /**
     * 엔티티 저장 후 생성된 Key 값을 반환합니다.
     * @param K key type
     * @param entity entity to save
     * @param keyKlass class of entity key
     */
    suspend fun <K : Any> insert(entity: E, keyKlass: KClass<K>): K

    /**
     * 엔티티들을 insert 합니다.
     * @param entities entities to insert
     */
    suspend fun insertAll(entities: Iterable<E>): List<E>

    /**
     * 엔티티들을 insert 하고, 새로 발급받은 key 값을 반환합니다.
     * @param K
     * @param entities entities to insert
     * @param keyKlass Class for key of entity
     */
    suspend fun <K : Any> insertAll(entities: Iterable<E>, keyKlass: KClass<K>): List<K>

    suspend fun upsert(entity: E): E

    suspend fun upsertAll(entities: Iterable<E>): List<E>

    suspend fun refresh(entity: E): E

    suspend fun refreshAllProperties(entity: E): E

    suspend fun refreshAll(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E>

    suspend fun refreshAllEntities(entities: Iterable<E>, vararg attributes: Attribute<E, *>): List<E>

    suspend fun deleteInBatch(entities: Iterable<E>)

    suspend fun deleteAllInBatch(): Int

    suspend fun getOne(id: ID): E?

    override suspend fun <S : E> findAll(example: Example<S>, sort: Sort): List<S>
}