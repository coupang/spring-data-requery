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
import org.jetbrains.annotations.Nullable;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.data.requery.utils.Iterables;
import org.springframework.data.requery.utils.RequeryUtils;

import javax.validation.constraints.NotNull;
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
public interface RequeryOperations {

//    ApplicationContext getApplicationContext();

    EntityDataStore<Object> getDataStore();

    RequeryMappingContext getMappingContext();

    default EntityModel getEntityModel() {
        return RequeryUtils.getEntityModel(getDataStore());
    }

    @SuppressWarnings("unchecked")
    default <E> EntityContext<E> getEntityContext() {
        return RequeryUtils.getEntityContext(getDataStore());
    }

    default <E> Selection<? extends Result<E>> select(Class<E> entityType) {
        return getDataStore().select(entityType);
    }

    default <E> Selection<? extends Result<E>> select(Class<E> entityType, QueryAttribute<?, ?>... attributes) {
        return getDataStore().select(entityType, attributes);
    }

    default Selection<? extends Result<Tuple>> select(Expression<?>... expressions) {
        return getDataStore().select(expressions);
    }

    default <E, K> E findById(Class<E> entityType, K id) {
        return getDataStore().findByKey(entityType, id);
    }

    default <E> List<E> findAll(Class<E> entityType) {
        return getDataStore().select(entityType).get().toList();
    }

    default <E> E refresh(@NotNull E entity) {
        return getDataStore().refresh(entity);
    }

    default <E> E refresh(@NotNull E entity, Attribute<?, ?>... attributes) {
        return getDataStore().refresh(entity, attributes);
    }

    default <E> List<E> refresh(@NotNull Iterable<E> entities, Attribute<?, ?>... attributes) {
        return Iterables.toList(getDataStore().refresh(entities, attributes));
    }

    default <E> E refreshAllProperties(@NotNull E entity) {
        return getDataStore().refreshAll(entity);
    }

    default <E> E upsert(@NotNull E entity) {
        return getDataStore().upsert(entity);
    }

    default <E> List<E> upsertAll(@NotNull Iterable<E> entities) {
        return Iterables.toList(getDataStore().upsert(entities));
    }

    default <E> E insert(@NotNull E entity) {
        return getDataStore().insert(entity);
    }

    default <E, K> K insert(@NotNull E entity, Class<K> keyClass) {
        return getDataStore().insert(entity, keyClass);
    }

    default <E> Insertion<? extends Result<Tuple>> insert(Class<E> entityType) {
        return getDataStore().insert(entityType);
    }

    default <E> InsertInto<? extends Result<Tuple>> insert(Class<E> entityType, QueryAttribute<E, ?>... attributes) {
        return getDataStore().insert(entityType, attributes);
    }

    default <E> List<E> insertAll(@NotNull Iterable<E> entities) {
        return Iterables.toList(getDataStore().insert(entities));
    }

    default <E, K> List<K> insertAll(@NotNull Iterable<E> entities, Class<K> keyClass) {
        return Iterables.toList(getDataStore().insert(entities, keyClass));
    }

    default Update<? extends Scalar<Integer>> update() {
        return getDataStore().update();
    }

    default <E> E update(@NotNull E entity) {
        return getDataStore().update(entity);
    }

    default <E> E update(@NotNull E entity, Attribute<?, ?>... attributes) {
        return getDataStore().update(entity, attributes);
    }

    default <E> Update<? extends Scalar<Integer>> update(Class<E> entityType) {
        return getDataStore().update(entityType);
    }

    default <E> List<E> updateAll(@NotNull Iterable<E> entities) {
        return Iterables.toList(getDataStore().update(entities));
    }

    default <E> Deletion<? extends Scalar<Integer>> delete() {
        return getDataStore().delete();
    }

    default <E> Deletion<? extends Scalar<Integer>> delete(Class<E> entityType) {
        return getDataStore().delete(entityType);
    }

    default <E> void delete(E entity) {
        getDataStore().delete(entity);
    }

    default <E> void deleteAll(Iterable<E> entities) {
        getDataStore().delete(entities);
    }

    default <E> Integer deleteAll(Class<E> entityType) {
        return getDataStore().delete(entityType).get().value();
    }

    default <E> Selection<? extends Scalar<Integer>> count(Class<E> entityType) {
        return getDataStore().count(entityType);
    }

    default <E> Selection<? extends Scalar<Integer>> count(QueryAttribute<?, ?>... attributes) {
        return getDataStore().count(attributes);
    }

    @SuppressWarnings("unchecked")
    default <E> int count(Class<E> entityType, QueryElement<? extends Result<E>> whereClause) {
        QueryElement<?> query = RequeryUtils.applyWhereClause(unwrap(select(Count.count(entityType))), whereClause.getWhereElements());
        Tuple tuple = ((QueryElement<? extends Result<Tuple>>) query).get().first();
        return tuple.<Integer>get(0);
    }

    default <E> boolean exists(Class<E> entityType, QueryElement<? extends Result<E>> whereClause) {
        return whereClause.limit(1).get().firstOrNull() != null;
    }

    default Result<Tuple> raw(String query, Object... parameters) {
        return getDataStore().raw(query, parameters);
    }

    default <E> Result<E> raw(Class<E> entityType, String query, Object... parameters) {
        return getDataStore().raw(entityType, query, parameters);
    }

    default <V> V runInTransaction(Callable<V> callable) {
        return runInTransaction(callable, null);
    }

    <V> V runInTransaction(Callable<V> callable, @Nullable TransactionIsolation isolation);

    default <V> V withTransaction(Function<EntityDataStore<Object>, V> block) {
        return withTransaction(block, null);
    }

    <V> V withTransaction(Function<EntityDataStore<Object>, V> block, @Nullable TransactionIsolation isolation);
}
