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

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.Transient;
import org.springframework.data.requery.domain.AbstractAuditable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Set;

/**
 * AbstractAuditableUser
 *
 * @author debop
 * @since 18. 6. 25
 */
@Entity
public abstract class AbstractAuditableUser extends AbstractAuditable<Integer> {

    @Key
    @Generated
    protected Integer id;

    protected String firstname;

    @JunctionTable
    @ManyToMany(cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected Set<AbstractAuditableRole> roles;


    @Override
    @Transient
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("firstname", firstname);
    }

    private static final long serialVersionUID = 7846556717471318510L;
}
