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

import io.requery.Persistable;
import io.requery.Superclass;

import javax.annotation.Nullable;
import java.beans.Transient;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entity를 표현하는 최상위 추상 클래스
 *
 * @author debop
 * @since 18. 6. 4
 */
@Superclass
public abstract class AbstractPersistable<ID> extends AbstractValueObject implements Persistable, Serializable {

    @Nullable
    public abstract ID getId();

    @Transient
    @io.requery.Transient
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public boolean equals(@Nullable final Object other) {
        if (other == null)
            return false;

        if (other instanceof AbstractPersistable) {
            AbstractPersistable that = (AbstractPersistable) other;
            return (isNew() && that.isNew())
                   ? hashCode() == other.hashCode()
                   : Objects.equals(getId(), that.getId());

        }

        return false;
    }

    @Override
    public int hashCode() {
        return (getId() != null) ? getId().hashCode() : System.identityHashCode(this);
    }

    @Override
    @io.requery.Transient
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("id", getId());
    }

    private static final long serialVersionUID = 5460350519810267858L;
}
