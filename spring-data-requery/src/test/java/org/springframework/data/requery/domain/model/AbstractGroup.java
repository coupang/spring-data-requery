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

import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.OrderBy;
import io.requery.PostInsert;
import io.requery.PostLoad;
import io.requery.PostUpdate;
import io.requery.PreInsert;
import io.requery.Table;
import io.requery.Transient;
import io.requery.Version;
import io.requery.query.MutableResult;
import io.requery.query.Order;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.requery.converters.ByteArrayToBlobConverter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * org.springframework.data.requery.domain.model.AbstractBasicGroup
 *
 * @author debop
 * @since 18. 6. 4
 */
@Slf4j
@Getter
@Entity
@Table(name = "Groups")
public class AbstractGroup extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -4655139079217699310L;

    @Key
    @Generated
    protected Long id;

    protected String name;
    protected String description;
    protected GroupType type;

    @Version
    protected int version = 0;

    @Convert(ByteArrayToBlobConverter.class)
    protected byte[] picture;

    @JunctionTable(name = "Func_Group_Members")
    @ManyToMany
    @OrderBy("name")
    protected MutableResult<AbstractPerson> members;

    @JunctionTable(name = "Group_Owners")
    @ManyToMany
    @OrderBy(value = "name", order = Order.ASC)
    protected MutableResult<AbstractPerson> owners;

    protected LocalDateTime createdAt;

    @Transient
    protected String temporaryName;

    @PreInsert
    protected void onPreInsert() {
        createdAt = LocalDateTime.now();
    }

    @PostInsert
    @PostLoad
    @PostUpdate
    protected void onPostEvents() {
        log.debug("Group events. group={}", this);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("type", type);
    }
}
