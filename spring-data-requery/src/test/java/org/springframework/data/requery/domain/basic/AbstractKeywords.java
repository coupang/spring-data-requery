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

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Table;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;

/**
 * org.springframework.data.requery.domain.basic.AbstractKeywords
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
@Table(name = "basic_keyword")
public abstract class AbstractKeywords extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = -5401099992210202481L;

    @Key
    @Generated
    protected Integer id;

    protected String isNotAJvmKeyword;

    protected Boolean isNew;
    protected Boolean isDefault;
    protected String getAbstract;

}
