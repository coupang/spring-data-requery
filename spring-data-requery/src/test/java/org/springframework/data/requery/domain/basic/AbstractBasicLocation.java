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

package org.springframework.data.requery.domain.basic;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToOne;
import io.requery.Table;
import io.requery.Transient;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;

/**
 * org.springframework.data.requery.domain.basic.AbstractBasicLocation
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
@Table(name = "basic_address")
public abstract class AbstractBasicLocation extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -4665641548712152873L;

    @Key
    @Generated
    protected Long id;

    protected String line1;
    protected String line2;
    protected String line3;

    @Column(length = 5)
    protected String zip;

    @Column(length = 2)
    protected String countryCode;

    protected String city;

    protected String state;

    @OneToOne(mappedBy = "address")
    @Column(name = "basic_user")
    protected AbstractBasicUser user;

    @Transient
    protected String description;
}
