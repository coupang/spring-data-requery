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

package org.springframework.data.requery.domain.functional;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.ReferentialAction;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.requery.domain.AbstractPersistable;

/**
 * @author Diego on 2018. 6. 14..
 */
@Getter
@Setter
@Entity
public abstract class AbstractFuncParent extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -6251746413885600923L;

    @Key
    @Generated
    protected Long id;

    @Column
    protected String name;

    @ManyToOne(cascade = { CascadeAction.DELETE })
    @ForeignKey(delete = ReferentialAction.SET_NULL, update = ReferentialAction.RESTRICT)
    protected AbstractFuncChild child;
}
