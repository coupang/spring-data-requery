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
import io.requery.Key;
import io.requery.ManyToOne;
import org.springframework.data.requery.domain.AbstractValueObject;

/**
 * AbstractItemSite
 *
 * @author debop
 * @since 18. 6. 25
 */
@Entity
public abstract class AbstractItemSite extends AbstractValueObject {

    @Key
    @ManyToOne
    protected AbstractItem item;

    @Key
    @ManyToOne
    protected AbstractSite site;


    public AbstractItemSite() {}

    public AbstractItemSite(AbstractItem item, AbstractSite site) {
        this.item = item;
        this.site = site;
    }


    private static final long serialVersionUID = -8045801232998470442L;
}
