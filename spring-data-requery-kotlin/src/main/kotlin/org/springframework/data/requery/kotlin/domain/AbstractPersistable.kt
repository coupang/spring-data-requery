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

package org.springframework.data.requery.kotlin.domain

import io.requery.Persistable
import io.requery.Superclass
import java.io.Serializable

/**
 * Abstract Entity class for Requery
 *
 * @author debop
 */
@Superclass
abstract class AbstractPersistable<ID> : AbstractValueObject(), Persistable, Serializable {

    abstract val id: ID

    val isNew: Boolean get() = (id == null)

    override fun equals(other: Any?): Boolean = when(other) {
        is AbstractPersistable<*> -> {
            if(isNew && other.isNew) hashCode() == other.hashCode()
            else id == other.id
        }
        else -> false
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    // NOTE: buildStringHelder 도 메소드이므로 @Transient 를 꼭 지정해줘야 한다.
    @io.requery.Transient
    override fun buildStringHelper(): ToStringBuilder {
        return super.buildStringHelper()
            .add("id", id)
    }
}