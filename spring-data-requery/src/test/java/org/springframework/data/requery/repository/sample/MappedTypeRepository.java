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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.requery.domain.sample.AbstractMappedType;
import org.springframework.data.requery.repository.RequeryRepository;

import java.util.List;

/**
 * MappedTypeRepository
 *
 * @author debop
 * @since 18. 6. 25
 */
@NoRepositoryBean
public interface MappedTypeRepository<T extends AbstractMappedType> extends RequeryRepository<T, Long> {

    // TODO: Spring EL을 이용하여 TABLE Name을 지정할 수 있으면 좋을까?
    // @Query("from #{#entityName} t where t.attribute1=?")
    List<T> findAllByAttribute1(String attribute1);

    // TODO: Spring EL을 이용하여 TABLE Name을 지정할 수 있으면 좋을까?
    // @Query("from #{#entityName} t where t.attribute1=?")
    Page<T> findByAttribute1Custom(String attribute1, Pageable pageable);
}
