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
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;

import java.util.Set;

/**
 * AbstractParent
 *
 * @author debop
 * @since 18. 6. 25
 */
@Getter
@Entity
public abstract class AbstractParent extends AbstractPersistable<Long> {

    @Key
    @Generated
    protected Long id;

    @JunctionTable
    @ManyToMany
    protected Set<Child> children;

    protected String name;

    public Parent addChild(Child child) {
        this.getChildren().add(child);
        return (Parent) this;
    }

    private static final long serialVersionUID = 7310565121245237400L;
}
