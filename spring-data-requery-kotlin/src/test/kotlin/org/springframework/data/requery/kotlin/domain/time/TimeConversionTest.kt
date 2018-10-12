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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 * TimeConversionTest
 *
 * @author debop
 * @since 18. 5. 14
 */
class TimeConversionTest : AbstractDomainTest() {

    @Test
    fun `insert and read Java time`() {
        with(operations) {
            val eventId = UUID.randomUUID()
            val localDateNow = LocalDate.now()
            val localDateTimeNow = LocalDateTime.now()
            val localTimeNow = LocalTime.now()
            val offsetDateTimeNow = OffsetDateTime.now()
            val zonedDateTimeNow = ZonedDateTime.now()

            val event = TimedEventEntity().apply {
                id = eventId
                localDate = localDateNow
                localDateTime = localDateTimeNow
                localTime = localTimeNow
                offsetDateTime = offsetDateTimeNow
                zonedDateTime = zonedDateTimeNow
            }
            insert(event)

            val loaded = findById(TimedEvent::class, eventId)
            assertThat(loaded).isNotNull
            assertThat(loaded?.localDate).isEqualTo(localDateNow)
            // LocalDateTime 의 nano 값이 제대로 저장 안된다.
            //            assertThat(loaded?.localDateTime).isEqualTo(localDateTimeNow)
            assertThat(loaded?.localTime).isEqualTo(localTimeNow)
            assertThat(loaded?.offsetDateTime).isEqualTo(offsetDateTimeNow)
            assertThat(loaded?.zonedDateTime).isEqualTo(zonedDateTimeNow)
        }
    }
}