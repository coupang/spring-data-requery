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

/**
 * org.springframework.data.requery.domain.AbstractValueObject
 *
 * @author debop
 */
public class AbstractValueObject implements ValueObject, Persistable {

    private static final long serialVersionUID = 7365523660535037710L;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return buildStringHelper().toString();
    }

    public String toString(int limit) {
        return buildStringHelper().toString(limit);
    }

    protected ToStringBuilder buildStringHelper() {
        return ToStringBuilder.of(this);
    }
}
