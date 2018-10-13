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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.data.requery.core.RequeryOperations;

import java.util.List;

/**
 * RequeryRepository
 *
 * @author debop
 * @since 18. 6. 4
 */
@NoRepositoryBean
public interface RequeryRepository<T, ID>
    extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T>, RequeryWhereExecutor<T> {

    @Autowired
    RequeryOperations getOperations();

    @Override
    @NotNull List<T> findAll();

    @Override
    @NotNull List<T> findAll(@NotNull final Sort sort);

    @Override
    @NotNull List<T> findAllById(@NotNull final Iterable<ID> ids);

    @Override
    <S extends T> @NotNull List<S> saveAll(@NotNull final Iterable<S> entities);

    <S extends T> @NotNull S insert(@NotNull final S entity);

    <S extends T, K> @NotNull K insert(@NotNull final S entity, @NotNull final Class<K> keyClass);

    <S extends T> @NotNull List<S> insert(@NotNull final Iterable<S> entities);

    <S extends T, K> @NotNull List<K> insert(@NotNull final Iterable<S> entities, @NotNull final Class<K> keyClass);

    <S extends T> @NotNull S upsert(@NotNull final S entity);

    <S extends T> @NotNull List<S> upsertAll(@NotNull final Iterable<S> entities);

    @SuppressWarnings("UnusedReturnValue")
    <S extends T> @NotNull S refresh(@NotNull final S entity);

    @SuppressWarnings("unchecked")
    <S extends T> @NotNull List<S> refresh(@NotNull final Iterable<S> entities, final Attribute<S, ?>... attributes);

    <S extends T> @NotNull S refreshAll(@NotNull final S entity);

    void deleteInBatch(@NotNull final Iterable<T> entities);

    int deleteAllInBatch();

    @Nullable T getOne(@NotNull final ID id);

    @Override
    <S extends T> @NotNull List<S> findAll(@NotNull final Example<S> example);

    @Override
    <S extends T> @NotNull List<S> findAll(@NotNull final Example<S> example, @NotNull final Sort sort);

}
