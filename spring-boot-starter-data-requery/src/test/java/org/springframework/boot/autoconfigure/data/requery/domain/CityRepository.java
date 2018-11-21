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

package org.springframework.boot.autoconfigure.data.requery.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Repository for {@link City}
 *
 * @author debop
 */
@Transactional(readOnly = true)
public interface CityRepository extends RequeryRepository<City, Long> {

    @Nonnull
    Page<City> findByNameLikeAndCountryLikeAllIgnoringCase(@Nonnull final String name,
                                                           @Nonnull final String country,
                                                           @Nonnull final Pageable pageable);

    @Nullable
    City findByNameAndCountryAllIgnoringCase(@Nonnull final String name, @Nonnull final String country);

    @Nullable
    City findByIdAndDeletedFalse(long cityId);

    @Nullable
    City findFirstByName(String cityName);
}
