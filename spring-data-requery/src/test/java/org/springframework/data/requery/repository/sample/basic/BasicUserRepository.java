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

package org.springframework.data.requery.repository.sample.basic;

import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * org.springframework.data.requery.repository.sample.basic.BasicUserRepository
 *
 * @author debop
 * @since 18. 6. 9
 */
public interface BasicUserRepository extends RequeryRepository<BasicUser, Long> {

    @Transactional(readOnly = true)
    @Override
    Optional<BasicUser> findById(Long primaryKey);


    @Transactional(readOnly = true)
    default List<BasicUser> findAllByName(String name) {
        return getOperations()
            .select(BasicUser.class)
            .where(BasicUser.NAME.eq(name))
            .get()
            .toList();
    }

    @Transactional(readOnly = true)
    default BasicUser findByEmail(String email) {
        return getOperations()
            .select(BasicUser.class)
            .where(BasicUser.EMAIL.eq(email))
            .get()
            .firstOrNull();
    }

    @Transactional(readOnly = true)
    @Query("select * from basic_user u where u.email = ?")
    BasicUser findByAnnotatedQuery(String email);
}
