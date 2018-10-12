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
import java.util.*

/**
 * org.springframework.data.requery.kotlin.domain.ToStringBuilder
 *
 * @author debop
 */
class ToStringBuilder(val className: String) : Serializable {

    companion object {
        @JvmStatic
        fun of(className: String): ToStringBuilder = ToStringBuilder(className)

        @JvmStatic
        fun of(obj: Any): ToStringBuilder = ToStringBuilder(obj)
    }

    constructor(obj: Any) : this(obj::class.java.simpleName)

    init {
        check(className.isNotBlank()) { "className must not be null or empty." }
    }

    private val map = HashMap<String, Any?>()
    private var cachedToString: String? = null

    private fun toStringValue(limit: Int): String {
        if(cachedToString == null) {
            val props = map.entries
                .joinToString(separator = ",", limit = limit) {
                    "${it.key}=${it.value.asString()}"
                }
            cachedToString = "$className($props)"
        }
        return cachedToString!!
    }

    private fun Any?.asString(): String = this?.toString() ?: "<null>"

    fun add(name: String, value: Any?): ToStringBuilder = apply {
        map[name] = value?.toString() ?: ""
    }

    override fun toString(): String = toStringValue(-1)

    fun toString(limit: Int): String = toStringValue(limit)

}