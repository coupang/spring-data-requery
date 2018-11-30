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

package org.springframework.data.requery.core;

import io.requery.TransactionIsolation;
import io.requery.meta.Attribute;
import io.requery.meta.EntityModel;
import io.requery.meta.QueryAttribute;
import io.requery.query.Condition;
import io.requery.query.Deletion;
import io.requery.query.Expression;
import io.requery.query.InsertInto;
import io.requery.query.Insertion;
import io.requery.query.Result;
import io.requery.query.Scalar;
import io.requery.query.Selection;
import io.requery.query.Tuple;
import io.requery.query.Update;
import io.requery.query.element.QueryElement;
import io.requery.query.function.Count;
import io.requery.sql.EntityContext;
import io.requery.sql.EntityDataStore;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.data.requery.utils.Iterables;
import org.springframework.data.requery.utils.RequeryUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * Java용 RequeryOperations
 *
 * @author debop
 * @since 18. 6. 4
 */
@ParametersAreNonnullByDefault
public interface RequeryOperations {

    @Nonnull
    EntityDataStore<Object> getDataStore();

    @Nonnull
    RequeryMappingContext getMappingContext();

    default EntityModel getEntityModel() {
        return RequeryUtils.getEntityModel(getDataStore());
    }

    @SuppressWarnings("unchecked")
    default <E> EntityContext<E> getEntityContext() {
        return RequeryUtils.getEntityContext(getDataStore());
    }

    default <E> Selection<? extends Result<E>> select(@Nonnull final Class<E> entityType) {
        return getDataStore().select(entityType);
    }

    default <E> Selection<? extends Result<E>> select(@Nonnull final Class<E> entityType,
                                                      @Nonnull final QueryAttribute<?, ?>... attributes) {
        return getDataStore().select(entityType, attributes);
    }

    default Selection<? extends Result<Tuple>> select(@Nonnull final Expression<?>... expressions) {
        return getDataStore().select(expressions);
    }

    default <E, K> E findById(@Nonnull final Class<E> entityType, @Nonnull final K id) {
        return getDataStore().findByKey(entityType, id);
    }

    default <E> List<E> findAll(@Nonnull final Class<E> entityType) {
        return getDataStore().select(entityType).get().toList();
    }

    default <E> E refresh(@Nonnull final E entity) {
        return getDataStore().refresh(entity);
    }

    @SuppressWarnings("UnusedReturnValue")
    default <E> E refresh(@Nonnull final E entity, @Nullable final Attribute<?, ?>... attributes) {
        return getDataStore().refresh(entity, attributes);
    }

    default <E> List<E> refresh(@Nonnull final Iterable<E> entities, final Attribute<?, ?>... attributes) {
        return Iterables.toList(getDataStore().refresh(entities, attributes));
    }

    default <E> E refreshAllProperties(@Nonnull final E entity) {
        return getDataStore().refreshAll(entity);
    }

    default <E> E upsert(@Nonnull final E entity) {
        return getDataStore().upsert(entity);
    }

    default <E> List<E> upsertAll(@Nonnull final Iterable<E> entities) {
        return Iterables.toList(getDataStore().upsert(entities));
    }

    default <E> E insert(@Nonnull final E entity) {
        return getDataStore().insert(entity);
    }

    default <E, K> K insert(@Nonnull final E entity, @Nonnull final Class<K> keyClass) {
        return getDataStore().insert(entity, keyClass);
    }

    default <E> Insertion<? extends Result<Tuple>> insert(@Nonnull final Class<E> entityType) {
        return getDataStore().insert(entityType);
    }

    @SuppressWarnings("unchecked")
    default <E> InsertInto<? extends Result<Tuple>> insert(@Nonnull final Class<E> entityType, QueryAttribute<E, ?>... attributes) {
        return getDataStore().insert(entityType, attributes);
    }

    default <E> List<E> insertAll(@Nonnull final Iterable<E> entities) {
        return Iterables.toList(getDataStore().insert(entities));
    }

    default <E, K> List<K> insertAll(@Nonnull final Iterable<E> entities, @Nonnull final Class<K> keyClass) {
        return Iterables.toList(getDataStore().insert(entities, keyClass));
    }

    @Nonnull
    default Update<? extends Scalar<Integer>> update() {
        return getDataStore().update();
    }

    default <E> E update(@Nonnull final E entity) {
        return getDataStore().update(entity);
    }

    default <E> E update(@Nonnull final E entity, final Attribute<?, ?>... attributes) {
        return getDataStore().update(entity, attributes);
    }

    default <E> Update<? extends Scalar<Integer>> update(@Nonnull final Class<E> entityType) {
        return getDataStore().update(entityType);
    }

    default <E> List<E> updateAll(@Nonnull final Iterable<E> entities) {
        return Iterables.toList(getDataStore().update(entities));
    }

    default Deletion<? extends Scalar<Integer>> delete() {
        return getDataStore().delete();
    }

    default <E> Deletion<? extends Scalar<Integer>> delete(@Nonnull final Class<E> entityType) {
        return getDataStore().delete(entityType);
    }

    default <E> void delete(@Nonnull final E entity) {
        getDataStore().delete(entity);
    }

    default <E> void deleteAll(@Nonnull final Iterable<E> entities) {
        getDataStore().delete(entities);
    }

    default <E> Integer deleteAll(@Nonnull final Class<E> entityType) {
        return getDataStore().delete(entityType).get().value();
    }

    default <E> Selection<? extends Scalar<Integer>> count(@Nonnull final Class<E> entityType) {
        return getDataStore().count(entityType);
    }

    default Selection<? extends Scalar<Integer>> count(final QueryAttribute<?, ?>... attributes) {
        return getDataStore().count(attributes);
    }

    @SuppressWarnings("unchecked")
    default <E> int count(@Nonnull final Class<E> entityType,
                          @Nonnull final QueryElement<? extends Result<E>> whereClause) {
        QueryElement<?> query = RequeryUtils.applyWhereClause(unwrap(select(Count.count(entityType))), whereClause.getWhereElements());
        Tuple tuple = ((QueryElement<? extends Result<Tuple>>) query).get().first();
        return tuple.<Integer>get(0);
    }

    default <E> boolean exists(@Nonnull final Class<E> entityType,
                               @Nonnull final QueryElement<? extends Result<E>> whereClause) {
        return whereClause.limit(1).get().firstOrNull() != null;
    }

    default <E, K> boolean existsBy(@Nonnull final Class<E> entityType,
                                    @Nonnull final Condition<K, ?> condition) {
        return getDataStore().count(entityType).from(entityType).where(condition).get().value() > 0;
    }

    default Result<Tuple> raw(@Nonnull final String query, final Object... parameters) {
        return getDataStore().raw(query, parameters);
    }

    default <E> Result<E> raw(@Nonnull final Class<E> entityType,
                              @Nonnull final String query,
                              final Object... parameters) {
        return getDataStore().raw(entityType, query, parameters);
    }

    default <V> V runInTransaction(@Nonnull final Callable<V> callable) {
        return runInTransaction(callable, null);
    }

    <V> V runInTransaction(@Nonnull final Callable<V> callable, @Nullable final TransactionIsolation isolation);

    default <V> V withTransaction(@Nonnull final Function<EntityDataStore<Object>, V> block) {
        return withTransaction(block, null);
    }

    <V> V withTransaction(@Nonnull final Function<EntityDataStore<Object>, V> block, @Nullable final TransactionIsolation isolation);
}
