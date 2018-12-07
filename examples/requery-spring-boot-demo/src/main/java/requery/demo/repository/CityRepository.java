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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.requery.repository.RequeryRepository;
import requery.demo.domain.City;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * CityRepository
 *
 * @author debop
 * @since 18. 10. 31
 */
@ParametersAreNonnullByDefault
public interface CityRepository extends RequeryRepository<City, Long> {

    @Nonnull
    Page<City> findByNameLikeAndCountryLikeAllIgnoringCase(final String name,
                                                           final String country,
                                                           final Pageable pageable);

    @Nullable
    City findByNameAndCountryAllIgnoringCase(final String name, final String country);

    @Nullable
    City findByIdAndDeletedFalse(long cityId);

    @Nullable
    City findFirstByName(String cityName);
}
