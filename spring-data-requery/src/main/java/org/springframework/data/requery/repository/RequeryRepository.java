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

package org.springframework.data.requery.repository;

import io.requery.meta.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.data.requery.core.RequeryOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * RequeryRepository
 *
 * @author debop
 * @since 18. 6. 4
 */
@NoRepositoryBean
@ParametersAreNonnullByDefault
public interface RequeryRepository<T, ID>
    extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T>, RequeryWhereExecutor<T> {

    @Autowired
    RequeryOperations getOperations();

    @Override
    @Nonnull
    List<T> findAll();

    @Override
    @Nonnull
    List<T> findAll(@Nonnull final Sort sort);

    @Override
    @Nonnull
    List<T> findAllById(@Nonnull final Iterable<ID> ids);

    @Override
    @Nonnull
    <S extends T> List<S> saveAll(@Nonnull final Iterable<S> entities);

    @Nonnull
    <S extends T> S insert(@Nonnull final S entity);

    @Nonnull
    <S extends T, K> K insert(@Nonnull final S entity, @Nonnull final Class<K> keyClass);

    @Nonnull
    <S extends T> List<S> insert(@Nonnull final Iterable<S> entities);

    @Nonnull
    <S extends T, K> List<K> insert(@Nonnull final Iterable<S> entities, @Nonnull final Class<K> keyClass);

    @Nonnull
    <S extends T> S upsert(@Nonnull final S entity);

    @Nonnull
    <S extends T> List<S> upsertAll(@Nonnull final Iterable<S> entities);

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    <S extends T> S refresh(@Nonnull final S entity);

    @SuppressWarnings({ "unchecked", "NullableProblems" })
    @Nonnull
    <S extends T> List<S> refresh(@Nonnull final Iterable<S> entities, final Attribute<S, ?>... attributes);

    @Nonnull
    <S extends T> S refreshAll(@Nonnull final S entity);

    void deleteInBatch(@Nonnull final Iterable<T> entities);

    int deleteAllInBatch();

    @Nullable
    T getOne(@Nonnull final ID id);

    @Override
    @Nonnull
    <S extends T> List<S> findAll(@Nonnull final Example<S> example);

    @Override
    @Nonnull
    <S extends T> List<S> findAll(@Nonnull final Example<S> example, @Nonnull final Sort sort);

}
