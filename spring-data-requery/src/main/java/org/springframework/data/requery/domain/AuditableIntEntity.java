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

package org.springframework.data.requery.domain;

import io.requery.Convert;
import io.requery.Lazy;
import io.requery.PreInsert;
import io.requery.PreUpdate;
import io.requery.Superclass;
import io.requery.converter.LocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * Auditable 이면서 Identifier 수형이 Integer인 Entity
 *
 * @author debop
 * @since 18. 6. 4
 */
@Superclass
public abstract class AuditableIntEntity extends AbstractPersistable<Integer> {

    @Convert(LocalDateTimeConverter.class)
    protected LocalDateTime createdDate;

    @Lazy
    @Convert(LocalDateTimeConverter.class)
    protected LocalDateTime lastModifiedDate;


    @PreInsert
    protected void onPreInsert() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }

    private static final long serialVersionUID = -4615850184590836079L;
}
