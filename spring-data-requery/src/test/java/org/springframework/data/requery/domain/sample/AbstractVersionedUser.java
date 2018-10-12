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

package org.springframework.data.requery.domain.sample;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Transient;
import io.requery.Version;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Date;
import java.util.Objects;

/**
 * AbstractVersionedUser
 *
 * @author debop
 * @since 18. 6. 25
 */
@Entity
public abstract class AbstractVersionedUser extends AbstractPersistable<Long> {

    @Key
    @Generated
    protected Long id;

    @Version
    protected Long version;


    protected String name;

    protected String email;

    protected Date birthday;

    public AbstractVersionedUser() {}

    public AbstractVersionedUser(String name, String email, Date birthday) {
        this.name = name;
        this.email = email;
        this.birthday = birthday;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, birthday);
    }

    @Transient
    @Override
    protected @NotNull ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("email", email)
            .add("birthday", birthday);
    }

    private static final long serialVersionUID = -7900602652543174851L;
}
