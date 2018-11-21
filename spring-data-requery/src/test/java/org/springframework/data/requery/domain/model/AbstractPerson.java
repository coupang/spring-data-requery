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
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Index;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.Lazy;
import io.requery.ManyToMany;
import io.requery.Naming;
import io.requery.Nullable;
import io.requery.OneToMany;
import io.requery.OneToOne;
import io.requery.PostDelete;
import io.requery.PostInsert;
import io.requery.PostLoad;
import io.requery.PostUpdate;
import io.requery.PreDelete;
import io.requery.PreInsert;
import io.requery.PreUpdate;
import io.requery.Transient;
import io.requery.converter.LocalDateConverter;
import io.requery.query.MutableResult;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.EntityState;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * org.springframework.data.requery.domain.model.AbstractPerson
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
public class AbstractPerson extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -2176683616253797032L;

    @Key
    @Generated
    @Column(name = "personId")
    protected Long id;

    @Index(value = "idx_person_name_email")
    protected String name;

    @Index(value = "idx_person_name_email")
    protected String email;

    @Convert(LocalDateConverter.class)
    protected LocalDate birthday;

    @Column(value = "'empty'")
    protected String description;

    @Nullable
    protected Integer age;

    @ForeignKey
    @OneToOne(cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected AbstractAddress address;

    @OneToMany(mappedBy = "owner", cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected MutableResult<AbstractPhone> phoneNumbers;

    @OneToMany
    protected Set<AbstractPhone> phoneNumberSet;

    @OneToMany
    protected List<AbstractPhone> phoneNumberList;

    @ManyToMany(mappedBy = "members")
    protected MutableResult<AbstractGroup> groups;

    @ManyToMany(mappedBy = "owners")
    protected MutableResult<AbstractGroup> ownedGroups;

    @JunctionTable(name = "Person_Friends")
    @ManyToMany(mappedBy = "personId")
    protected Set<AbstractPerson> friends;

    @Lazy
    protected String about;

    @Column(unique = true)
    @Naming(getter = "getUUID", setter = "setUUID")
    protected UUID uuid;

    protected URL homepage;

    protected String picture;

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("email", email);
    }

    @Getter
    private EntityState previous;

    @Getter
    private EntityState current;

    private void setState(EntityState state) {
        this.previous = current;
        this.current = state;
    }

    @PreInsert
    public void onPreInsert() {
        setState(EntityState.PRE_SAVE);
    }

    @PostInsert
    public void onPostInsert() {
        setState(EntityState.POST_SAVE);
    }

    @PostLoad
    public void onPostLoad() {
        setState(EntityState.POST_LOAD);
    }

    @PreUpdate
    public void onPreUpdate() {
        setState(EntityState.PRE_UPDATE);
    }

    @PostUpdate
    public void onPostUpdate() {
        setState(EntityState.POST_UPDATE);
    }

    @PreDelete
    public void onPreDelete() {
        setState(EntityState.PRE_DELETE);
    }

    @PostDelete
    public void onPostDelete() {
        setState(EntityState.POST_DELETE);
    }
}
