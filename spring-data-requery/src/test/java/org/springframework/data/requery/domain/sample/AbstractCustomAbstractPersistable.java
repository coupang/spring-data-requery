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
import io.requery.Table;
import org.springframework.data.requery.domain.AbstractPersistable;

/**
 * @author Diego on 2018. 6. 13..
 */
@Entity
@Table(name = "customAbstractPersistable")
public abstract class AbstractCustomAbstractPersistable extends AbstractPersistable<Long> {

    @Key
    @Generated
    protected Long id;

    // NOTE: Id 만 있는 Entity는 Insert 구문이 제대로 생성되지 않습니다.
    protected String attr;

    private static final long serialVersionUID = -1509421819757268522L;
}
