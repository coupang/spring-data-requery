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

package org.springframework.data.requery.repository.sample;

import io.requery.query.Result;
import io.requery.query.Return;
import org.springframework.data.requery.domain.sample.Role;
import org.springframework.data.requery.repository.RequeryRepository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * RoleRepository
 *
 * @author debop
 * @since 18. 6. 12
 */
public interface RoleRepository extends RequeryRepository<Role, Integer> {

    @Nonnull
    List<Role> findAll();

    @Nonnull
    Optional<Role> findById(@Nonnull final Integer id);


    @Override
    @Nonnull
    Optional<Role> findOne(@Nonnull final Return<? extends Result<Role>> whereClause);


    Long countByName(String name);
}
