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

package org.springframework.data.requery.repository.support;

import io.requery.meta.Attribute;
import io.requery.query.Condition;
import io.requery.query.LogicalCondition;
import io.requery.query.NamedExpression;
import io.requery.query.OrderingExpression;
import io.requery.query.Result;
import io.requery.query.Return;
import io.requery.query.Tuple;
import io.requery.query.element.QueryElement;
import io.requery.query.function.Count;
import io.requery.sql.EntityDataStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.repository.query.QueryByExampleBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.requery.utils.RequeryUtils.applyPageable;
import static org.springframework.data.requery.utils.RequeryUtils.applySort;
import static org.springframework.data.requery.utils.RequeryUtils.foldConditions;
import static org.springframework.data.requery.utils.RequeryUtils.getKeyExpression;
import static org.springframework.data.requery.utils.RequeryUtils.getOrderingExpressions;
import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * Default implementation of the {@link org.springframework.data.repository.CrudRepository} interface.
 * This will offer you a more sophisticated interface than the plain {@link EntityDataStore}.
 *
 * @author debop
 * @since 18. 6. 4
 */
@SuppressWarnings("ConstantConditions")
@Slf4j
@Repository
@Transactional(readOnly = true)
public class SimpleRequeryRepository<T, ID> implements RequeryRepositoryImplementation<T, ID> {

    @Getter
    @Nonnull private final RequeryOperations operations;
    @Nonnull private final RequeryEntityInformation<T, ID> entityInformation;

    @Nullable private final Class<T> domainClass;
    @Nonnull private final String domainClassName;

    @Nullable private CrudMethodMetadata crudMethodMetadata;

    public SimpleRequeryRepository(@Nonnull final RequeryEntityInformation<T, ID> entityInformation,
                                   @Nonnull final RequeryOperations operations) {
        log.debug("Create SimpleRequeryRepository. domainClass={}", entityInformation.getJavaType());

        this.entityInformation = entityInformation;
        this.domainClass = entityInformation.getJavaType();
        this.domainClassName = (domainClass != null) ? domainClass.getSimpleName() : "Unknown";
        this.operations = operations;
    }

    @Override
    public void setRepositoryMethodMetadata(@Nullable final CrudMethodMetadata crudMethodMetadata) {
        this.crudMethodMetadata = crudMethodMetadata;
    }

    @Transactional
    @Override
    @Nonnull
    public <S extends T> S insert(@Nonnull final S entity) {
        return operations.insert(entity);
    }

    @Transactional
    @Override
    @Nonnull
    public <S extends T, K> K insert(@Nonnull final S entity, @Nonnull final Class<K> keyClass) {
        return operations.insert(entity, keyClass);
    }

    @Transactional
    @Override
    @Nonnull
    public <S extends T> List<S> insert(@Nonnull final Iterable<S> entities) {
        return operations.insertAll(entities);
    }

    @Transactional
    @Override
    @Nonnull
    public <S extends T, K> List<K> insert(@Nonnull final Iterable<S> entities, @Nonnull final Class<K> keyClass) {
        return operations.insertAll(entities, keyClass);
    }

    @Transactional
    @Override
    @Nonnull
    public <S extends T> S upsert(@Nonnull final S entity) {
        return operations.upsert(entity);
    }

    @Transactional
    @Override
    @Nonnull
    public <S extends T> List<S> upsertAll(@Nonnull final Iterable<S> entities) {
        return operations.upsertAll(entities);
    }

    @Nonnull
    public <S extends T> S refresh(@Nonnull final S entity) {
        return operations.refresh(entity);
    }

    @SuppressWarnings({ "unchecked", "NullableProblems" })
    @Nonnull
    public <S extends T> List<S> refresh(@Nonnull final Iterable<S> entities, final Attribute<S, ?>... attributes) {
        return operations.refresh(entities, attributes);
    }

    @Nonnull
    public <S extends T> S refreshAll(@Nonnull final S entity) {
        return operations.refreshAllProperties(entity);
    }

    @Transactional
    @Override
    public void deleteInBatch(@Nonnull final Iterable<T> entities) {
        operations.deleteAll(entities);
    }

    @Transactional
    @Override
    public int deleteAllInBatch() {
        return operations.delete(domainClass).get().value();
    }

    @Override
    public T getOne(@Nonnull final ID id) {
        return operations.findById(domainClass, id);
    }

    @Nonnull
    @Override
    public List<T> findAll(@Nonnull final Sort sort) {

        log.debug("Find all {} with sort, sort={}", domainClassName, sort);

        if (sort.isSorted()) {
            OrderingExpression<?>[] orderingExprs = getOrderingExpressions(domainClass, sort);

            if (orderingExprs.length > 0) {
                return operations
                    .select(domainClass)
                    .orderBy(orderingExprs)
                    .get()
                    .toList();
            }
        }

        return operations
            .select(domainClass)
            .get()
            .toList();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public Page<T> findAll(@Nonnull final Pageable pageable) {

        log.debug("Find all {} with paging, pageable={}", domainClassName, pageable);

        if (pageable.isPaged()) {
            QueryElement<? extends Result<T>> query = (QueryElement<? extends Result<T>>)
                applyPageable(domainClass,
                              (QueryElement<? extends Result<T>>) operations.select(domainClass),
                              pageable);
            List<T> content = query.get().toList();
            long total = operations.count(domainClass).get().value().longValue();

            return new PageImpl<>(content, pageable, total);
        } else {
            List<T> content = operations
                .select(domainClass)
                .get()
                .toList();
            return new PageImpl<>(content);
        }
    }

    @Transactional
    @Nonnull
    @Override
    public <S extends T> S save(@Nonnull final S entity) {
        return operations.upsert(entity);
    }

    @Transactional
    @Nonnull
    @Override
    public <S extends T> List<S> saveAll(@Nonnull final Iterable<S> entities) {
        return operations.upsertAll(entities);
    }

    @Nonnull
    @Override
    public Optional<T> findById(@Nonnull final ID id) {
        return Optional.ofNullable(operations.findById(domainClass, id));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean existsById(@Nonnull final ID id) {
        NamedExpression<ID> keyExpr = (NamedExpression<ID>) getKeyExpression(domainClass);

        Tuple result = operations
            .select(Count.count(domainClass))
            .where(keyExpr.eq(id))
            .get()
            .first();

        return result.<Integer>get(0) > 0;
    }

    @Nonnull
    @Override
    public List<T> findAll() {
        return operations.findAll(domainClass);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public List<T> findAllById(@Nonnull final Iterable<ID> ids) {
        HashSet<ID> idSet = new HashSet<>();
        for (ID id : ids) {
            idSet.add(id);
        }
        NamedExpression<ID> keyExpr = (NamedExpression<ID>) getKeyExpression(domainClass);

        return operations
            .select(domainClass)
            .where(keyExpr.in(idSet))
            .get()
            .toList();
    }

    @Override
    public long count() {
        return operations
            .count(domainClass)
            .get()
            .value()
            .longValue();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public void deleteById(@Nonnull final ID id) {
        log.debug("Delete {} entity by id. id={}", domainClassName, id);

        NamedExpression<ID> keyExpr = (NamedExpression<ID>) getKeyExpression(domainClass);

        Integer deletedCount = operations
            .delete(domainClass)
            .where(keyExpr.eq(id))
            .get()
            .value();

        log.debug("Deleted entity={}, count={}", domainClassName, deletedCount);
    }

    @Transactional
    @Override
    public void delete(@Nonnull final T entity) {
        operations.delete(entity);
    }

    @Transactional
    @Override
    public void deleteAll(@Nonnull final Iterable<? extends T> entities) {
        operations.deleteAll(entities);
    }

    @Transactional
    @Override
    public void deleteAll() {
        log.debug("Delete All entities. entity name={} ...", domainClassName);

        Integer deletedCount = operations.delete(domainClass).get().value();

        log.debug("Delete All entities. entity name={}, deleted count={}", domainClassName, deletedCount);
    }

    @Nonnull
    @Override
    public <S extends T> Optional<S> findOne(@Nonnull final Example<S> example) {
        return Optional.ofNullable(buildQueryByExample(example).get().firstOrNull());
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <S extends T> List<S> findAll(@Nonnull final Example<S> example) {
        return buildQueryByExample(example).get().toList();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <S extends T> List<S> findAll(@Nonnull final Example<S> example, @Nonnull final Sort sort) {
        QueryElement<?> query = applySort(domainClass,
                                          unwrap(buildQueryByExample(example)),
                                          sort);
        return ((QueryElement<? extends Result<S>>) query).get().toList();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <S extends T> Page<S> findAll(@Nonnull final Example<S> example, @Nonnull final Pageable pageable) {

        QueryElement<?> baseQuery = unwrap(buildQueryByExample(example));
        long total = count((QueryElement<? extends Result<T>>) baseQuery);

        QueryElement<?> query = applyPageable(domainClass,
                                              unwrap(buildQueryByExample(example)),
                                              pageable);
        return new PageImpl<>(((QueryElement<? extends Result<S>>) query).get().toList(),
                              pageable,
                              total);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends T> long count(@Nonnull Example<S> example) {
        return count((QueryElement<? extends Result<T>>) buildQueryByExample(example));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends T> boolean exists(@Nonnull Example<S> example) {
        return buildQueryByExample(example).limit(1).get().firstOrNull() != null;
    }

    @SuppressWarnings("unchecked")
    private <S extends T> QueryElement<? extends Result<S>> buildQueryByExample(@Nonnull Example<S> example) {
        QueryElement<? extends Result<S>> root = (QueryElement<? extends Result<S>>) unwrap(operations.select(domainClass));
        return QueryByExampleBuilder.applyExample(root, example);
    }

    @Nonnull
    @Override
    public Optional<T> findOne(@Nonnull final Return<? extends Result<T>> whereClause) {
        List<T> list = whereClause.get().toList();
        if (list.size() == 1) {
            return Optional.ofNullable(list.get(0));
        } else if (list.isEmpty()) {
            return Optional.empty();
        } else {
            throw new IncorrectResultSizeDataAccessException(1, list.size());
        }
    }

    @Nonnull
    @Override
    public List<T> findAll(@Nonnull final Return<? extends Result<T>> whereClause) {
        return whereClause.get().toList();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public Page<T> findAll(@Nonnull final QueryElement<? extends Result<T>> whereClause,
                           @Nonnull final Pageable pageable) {

        int total = count(whereClause);

        Return<?> query = applyPageable(domainClass, whereClause, pageable);
        List<T> contents = ((Return<? extends Result<T>>) query).get().toList();

        return new PageImpl<>(contents, pageable, total);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public List<T> findAll(@Nonnull final Iterable<Condition<T, ?>> conditions,
                           @Nonnull final Sort sort) {

        LogicalCondition<T, ?> condition = foldConditions(conditions);
        QueryElement<?> query = unwrap(operations.select(domainClass).where(condition));
        return ((QueryElement<? extends Result<T>>) applySort(domainClass, query, sort)).get().toList();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public List<T> findAll(@Nonnull final Iterable<Condition<T, ?>> conditions) {

        LogicalCondition<T, ?> condition = foldConditions(conditions);
        return operations.select(domainClass).where(condition).get().toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int count(@Nonnull final QueryElement<? extends Result<T>> whereClause) {
        return getOperations().count(domainClass, whereClause);
    }

    @Override
    public boolean exists(@Nonnull final QueryElement<? extends Result<T>> whereClause) {
        return getOperations().exists(domainClass, whereClause);
    }
}
