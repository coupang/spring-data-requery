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

package org.springframework.data.requery.kotlin.converters

import io.requery.Converter
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Convert [LocalDateTime] to [Long]
 * @author debop
 * @since 18. 7. 2
 */
class LocalDateTimeToLongConverter : Converter<LocalDateTime, Long> {

    override fun getPersistedType(): Class<Long> = Long::class.java
    override fun getMappedType(): Class<LocalDateTime> = LocalDateTime::class.java
    override fun getPersistedSize(): Int? = null

    override fun convertToMapped(type: Class<out LocalDateTime>?, value: Long?): LocalDateTime? {
        return value?.let {
            val epochSecond = it / NANO_IN_SECONDS
            val nano = it % NANO_IN_SECONDS
            return LocalDateTime.ofEpochSecond(epochSecond, nano.toInt(), ZoneOffset.UTC)
        }
    }

    override fun convertToPersisted(value: LocalDateTime?): Long? {
        return value?.let {
            it.toEpochSecond(ZoneOffset.UTC) * NANO_IN_SECONDS + it.nano
        }
    }
}