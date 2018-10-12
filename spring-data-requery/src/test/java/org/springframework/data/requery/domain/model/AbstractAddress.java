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

package org.springframework.data.requery.domain.model;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToOne;
import lombok.Getter;
import lombok.Setter;

/**
 * org.springframework.data.requery.domain.model.AbstractBasicLocation
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Setter
@Entity(copyable = true)
public abstract class AbstractAddress extends Coordinate {

    @Key
    @Generated
    protected int id;

    protected String line1;
    protected String line2;

    protected String state;

    @Column(length = 5)
    protected String zip;

    @Column(length = 2)
    protected String countryCode;

    protected String city;

    @OneToOne(mappedBy = "address", cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected AbstractPerson person;

    @Convert(AddressTypeConverter.class)
    protected AddressType type;

    @Override
    public String toString() {
        return "Don't override me";
    }
}
