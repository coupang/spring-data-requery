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

package org.springframework.data.requery.kotlin.domain.time


import io.requery.Column
import io.requery.Convert
import io.requery.Entity
import io.requery.Key
import org.springframework.data.requery.kotlin.converters.LocalDateTimeToLongConverter
import org.springframework.data.requery.kotlin.converters.LocalTimeToLongConverter
import org.springframework.data.requery.kotlin.converters.OffsetDateTimeToStringConverter
import org.springframework.data.requery.kotlin.converters.ZonedDateTimeToStringConverter
import org.springframework.data.requery.kotlin.domain.PersistableObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 * UpsertEvent
 *
 * @author debop
 * @since 18. 5. 14
 */
@Entity
interface TimedEvent : PersistableObject {

    @get:Key
    var id: UUID

    var name: String

    var localDate: LocalDate

    @get:Convert(LocalDateTimeToLongConverter::class)
    var localDateTime: LocalDateTime?

    @get:Column(name = "local_time")
    @get:Convert(LocalTimeToLongConverter::class)
    var localTime: LocalTime?

    @get:Convert(OffsetDateTimeToStringConverter::class)
    var offsetDateTime: OffsetDateTime?

    @get:Convert(ZonedDateTimeToStringConverter::class)
    var zonedDateTime: ZonedDateTime?

}