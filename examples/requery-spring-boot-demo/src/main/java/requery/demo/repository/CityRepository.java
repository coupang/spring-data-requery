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

package requery.demo.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.requery.repository.RequeryRepository;
import requery.demo.domain.City;

/**
 * CityRepository
 *
 * @author debop
 * @since 18. 10. 31
 */
public interface CityRepository extends RequeryRepository<City, Long> {

    @NotNull
    Page<City> findByNameLikeAndCountryLikeAllIgnoringCase(@NotNull final String name,
                                                           @NotNull final String country,
                                                           @NotNull final Pageable pageable);

    @Nullable
    City findByNameAndCountryAllIgnoringCase(@NotNull final String name, @NotNull final String country);

    @Nullable
    City findByIdAndDeletedFalse(long cityId);

    @Nullable
    City findFirstByName(String cityName);
}
