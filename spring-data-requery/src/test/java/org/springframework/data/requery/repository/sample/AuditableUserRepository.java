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

import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.domain.sample.AuditableUser;
import org.springframework.data.requery.repository.RequeryRepository;

import java.util.List;

/**
 * AuditableUserRepository
 *
 * @author debop
 * @since 18. 6. 12
 */
public interface AuditableUserRepository extends RequeryRepository<AuditableUser, Long> {

    List<AuditableUser> findByFirstname(final String firstname);

    // TODO : Spring EL 을 이용한 값 지정하기
    // a.lastModifiedDate = :#{T(org.springframework.data.jpa.util.FixedDate).INSTANCE.getDate()}

    @Query("update AuditableUser a set a.firstname = upper(a.firstname), a.lastModifiedDate = Date()")
    void udpateAllNamesToUpperCase();
}
