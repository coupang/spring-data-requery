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

import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.converters.IntArrayListToStringConverter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.ArrayList;
import java.util.Objects;

/**
 * org.springframework.data.requery.domain.model.AbstractPhone
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
@NoArgsConstructor
public abstract class AbstractPhone extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = -3682340011300104110L;

    public AbstractPhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AbstractPhone(String phoneNumber, boolean normalized) {
        this.phoneNumber = phoneNumber;
        this.normalized = normalized;
    }

    @Key
    @Generated
    protected Integer id;

    protected String phoneNumber;
    protected boolean normalized;

    @Column
    @Convert(IntArrayListToStringConverter.class)
    protected ArrayList<Integer> extensions;

    @ManyToOne
    protected AbstractPerson owner;

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, normalized);
    }

    @Transient
    @Override
    protected @NotNull ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("phoneNumber", phoneNumber)
            .add("normalized", normalized)
            .add("extensions", extensions);
    }
}
