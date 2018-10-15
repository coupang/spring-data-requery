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

import org.springframework.data.repository.NoRepositoryBean

/**
 * Interface for generic CRUD operations with Corotine on a repository for a specific type.
 *
 * @author debop
 * @since 18. 10. 14
 */
@NoRepositoryBean
interface CoroutineCrudRepository<E : Any, ID : Any> : CoroutineRepository<E, ID> {

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be null.
     * @return the saved entity will never be null.
     */
    suspend fun <S : E> save(entity: S): S

    /**
     * Saves all given entities.
     *
     * @param entities must not be null.
     * @return the saved entities will never be null.
     * @throws IllegalArgumentException in case the given entity is null.
     */
    suspend fun <S : E> saveAll(entities: Iterable<S>): Iterable<S>

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be null.
     * @return the entity with the given id or Optional#empty() if none found
     * @throws IllegalArgumentException if `id` is null.
     */
    suspend fun findById(id: ID): E?

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be null.
     * @return true if an entity with the given id exists, false otherwise.
     * @throws IllegalArgumentException if `id` is null.
     */
    suspend fun existsById(id: ID): Boolean

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    suspend fun findAll(): Iterable<E>

    /**
     * Returns all instances of the type with the given IDs.
     *
     * @param ids
     * @return
     */
    suspend fun findAllById(ids: Iterable<ID>): List<E>

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    suspend fun count(): Long

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be null.
     * @throws IllegalArgumentException in case the given `id` is null
     */
    suspend fun deleteById(id: ID)

    /**
     * Deletes a given entity.
     *
     * @param entity
     * @throws IllegalArgumentException in case the given entity is null.
     */
    suspend fun delete(entity: E)

    /**
     * Deletes the given entities.
     *
     * @param entities
     * @throws IllegalArgumentException in case the given [Iterable] is null.
     */
    suspend fun deleteAll(entities: Iterable<E>)

    /**
     * Deletes all entities managed by the repository.
     */
    suspend fun deleteAll()
}