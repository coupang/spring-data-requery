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
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import io.requery.OneToOne;
import io.requery.Persistable;
import io.requery.ReferentialAction;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Diego on 2018. 6. 14..
 */
@Getter
@Setter
@Entity(cacheable = false)
public abstract class AbstractFuncParentNoCascade implements Persistable {

    @Key
    protected Long id;

    @OneToOne(cascade = { CascadeAction.NONE })
    @ForeignKey(delete = ReferentialAction.SET_NULL, update = ReferentialAction.RESTRICT)
    protected AbstractFuncChildOneToOneNoCascade oneToOne;

    @ManyToOne(cascade = { CascadeAction.NONE })
    @ForeignKey(delete = ReferentialAction.SET_NULL, update = ReferentialAction.RESTRICT)
    protected AbstractFuncChildManyToOneNoCascade manyToOne;

    @OneToMany(cascade = { CascadeAction.NONE })
    protected List<AbstractFuncChildOneToManyNoCascade> oneToMany;

    @ManyToMany(cascade = { CascadeAction.NONE })
    @JunctionTable
    protected List<AbstractFuncChildManyToManyNoCascade> manyToMany;
}
