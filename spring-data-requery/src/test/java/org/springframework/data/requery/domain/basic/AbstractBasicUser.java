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
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.OneToOne;
import io.requery.Table;
import io.requery.Transient;
import lombok.Getter;
import org.springframework.data.requery.domain.AuditableLongEntity;
import org.springframework.data.requery.domain.ToStringBuilder;

import javax.annotation.Nonnull;
import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * AbstractBasicUser
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity(name = "BasicUser", copyable = true)
@Table(name = "basic_user")
public abstract class AbstractBasicUser extends AuditableLongEntity {

    @Key
    @Generated
    protected Long id;

    protected String name;
    protected String email;
    protected LocalDate birthday;
    protected Integer age;

    @ForeignKey
    @OneToOne
    protected AbstractBasicLocation address;

    @ManyToMany(mappedBy = "members")
    protected Set<AbstractBasicGroup> groups;

    protected String about;

    @Column(unique = true)
    protected UUID uuid;

    protected URL homepage;

    protected String picture;

    @Override
    public int hashCode() {
        return Objects.hash(name, email, birthday);
    }

    @Transient
    @Override
    @Nonnull
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("email", email)
            .add("birthday", birthday);
    }

    private static final long serialVersionUID = -2693264826800934057L;
}
