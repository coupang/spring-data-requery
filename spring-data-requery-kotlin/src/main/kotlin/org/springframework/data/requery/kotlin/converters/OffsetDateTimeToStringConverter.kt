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
import java.time.OffsetDateTime

/**
 * Convert [OffsetDateTime] to [String]
 * @author debop
 * @since 18. 7. 2
 */
class OffsetDateTimeToStringConverter : Converter<OffsetDateTime, String> {

    override fun getPersistedType(): Class<String> = String::class.java
    override fun getMappedType(): Class<OffsetDateTime> = OffsetDateTime::class.java
    override fun getPersistedSize(): Int? = null

    override fun convertToMapped(type: Class<out OffsetDateTime>?, value: String?): OffsetDateTime? {
        return value?.let { OffsetDateTime.parse(it) }
    }

    override fun convertToPersisted(value: OffsetDateTime?): String? {
        return value?.toIsoOffsetDateTimeString()
    }
}