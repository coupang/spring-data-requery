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

import java.io.Serializable

/**
 * 값 형식의 데이터를 표현하는 클래스의 기본 인터페이스입니다.
 */
interface ValueObject : Serializable

/**
 * 값 형식의 데이터를 표현하는 클래스의 추상화 클래스
 *
 * @author debop
 */
abstract class AbstractValueObject : ValueObject {

    override fun equals(other: Any?): Boolean = when(other) {
        null -> false
        else -> this === other || (this.javaClass == other.javaClass && hashCode() == other.hashCode())
    }

    override fun hashCode(): Int = System.identityHashCode(this)

    /**
     * Don't override toString queryMethod, override buildStringHelper instead
     */
    override fun toString(): String = buildStringHelper().toString()

    /**
     * Don't override toString queryMethod, override buildStringHelper instead
     */
    open fun toString(limit: Int): String = buildStringHelper().toString(limit)

    /**
     * build ToStringBuilder for object description string
     **/
    protected open fun buildStringHelper(): ToStringBuilder = ToStringBuilder.of(this)

}
