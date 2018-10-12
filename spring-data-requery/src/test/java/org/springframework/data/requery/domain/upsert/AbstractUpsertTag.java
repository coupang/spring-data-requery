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

package org.springframework.data.requery.domain.upsert;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Entity;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Table;
import io.requery.Transient;
import io.requery.query.MutableResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.UUID;

/**
 * @author Diego on 2018. 6. 10..
 */
@Getter
@Entity(name = "UpsertTag")
@Table(name = "upsert_tag")
public abstract class AbstractUpsertTag extends AbstractPersistable<UUID> {

    @Key
    protected UUID id;

    @Column
    protected String name;

    @ManyToMany(cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected MutableResult<AbstractUpsertEvent> events;

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : System.identityHashCode(this);
    }

    @Transient
    @Override
    protected @NotNull ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name);
    }
}
