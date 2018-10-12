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
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Table;
import io.requery.query.MutableResult;
import lombok.Getter;
import org.springframework.data.requery.converters.ByteArrayToBlobConverter;
import org.springframework.data.requery.domain.AbstractPersistable;

/**
 * org.springframework.data.requery.domain.basic.AbstractBasicGroup
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
@Table(name = "basic_group")
public abstract class AbstractBasicGroup extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 6213359648179473134L;

    @Key
    @Generated
    protected Integer id;

    @Column(unique = true)
    protected String name;

    protected String description;

    @Convert(ByteArrayToBlobConverter.class)
    protected byte[] picture;

    @JunctionTable
    @ManyToMany
    protected MutableResult<AbstractBasicUser> members;
}
