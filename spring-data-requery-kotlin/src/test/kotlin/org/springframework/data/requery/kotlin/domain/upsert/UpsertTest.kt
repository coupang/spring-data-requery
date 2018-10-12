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

package org.springframework.data.requery.kotlin.domain.upsert

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import java.util.*

/**
 * org.springframework.data.requery.kotlin.domain.upsert.UpsertTest
 *
 * @author debop
 */
class UpsertTest : AbstractDomainTest() {

    @Before
    fun setup() {
        with(operations) {
            deleteAll(Tag::class)
            deleteAll(UpsertEvent::class)
            deleteAll(Place::class)
            deleteAll(Location::class)
        }
    }

    @Test
    fun `insert OneToMany`() {
        val eventId = UUID.randomUUID()
        val event = UpsertEventEntity().apply {
            id = eventId
            name = "test"
        }

        val tag1 = TagEntity().apply { id = UUID.randomUUID(); name = "tag1" }
        val tag2 = TagEntity().apply { id = UUID.randomUUID(); name = "tag2" }

        event.tags.add(tag1)
        event.tags.add(tag2)

        with(operations) {
            insert(tag1)
            insert(tag2)
            insert(event)

            val loaded = findById(UpsertEvent::class, eventId)
            Assertions.assertThat(loaded).isNotNull
            Assertions.assertThat(loaded?.tags?.size).isEqualTo(2)
            Assertions.assertThat(loaded?.tags).containsOnly(tag1, tag2)

            Assertions.assertThat(select(Tag::class).get().toList().size).isGreaterThanOrEqualTo(2)
        }
    }

    @Test
    fun `upsert event`() {
        val eventId = UUID.randomUUID()
        val event = UpsertEventEntity().apply {
            id = eventId
            name = "test"
        }

        with(operations) {
            upsert(event)

            val found = findById(UpsertEvent::class, eventId)
            Assertions.assertThat(found?.id).isEqualTo(eventId)
        }
    }

    @Test
    fun `upsert one to many`() {
        val event = UpsertEventEntity().apply {
            id = UUID.randomUUID()
        }
        val place = PlaceEntity().apply {
            id = UUID.randomUUID().toString()
            name = "place"
            events += event
        }
        event.place = place

        with(operations) {
            upsert(place)

            val savedPlace = findById(Place::class, place.id)
            Assertions.assertThat(savedPlace?.id).isEqualTo(place.id)
            Assertions.assertThat(savedPlace?.events).hasSize(1)

            Assertions.assertThat(savedPlace?.events?.first()?.id).isEqualTo(event.id)
        }
    }

    @Test
    fun `upsert many to many`() {

        with(operations) {
            val event1 = UpsertEventEntity().apply { id = UUID.randomUUID() }
            val tag = TagEntity().apply {
                id = UUID.randomUUID()
                events += event1
            }

            upsert(tag)

            val event2 = UpsertEventEntity().apply { id = UUID.randomUUID() }
            tag.events += event2
            upsert(event2)
            upsert(tag)

            val savedTag = findById(Tag::class, tag.id)
            Assertions.assertThat(savedTag?.id).isEqualTo(tag.id)
            Assertions.assertThat(savedTag?.events?.size).isEqualTo(2)
        }
    }

    @Test
    fun `upsert insert one to many`() {
        with(operations) {
            val eventId = UUID.randomUUID()
            val event = UpsertEventEntity().apply { id = eventId }
            upsert(event)

            val event1 = UpsertEventEntity().apply { id = eventId }
            val place = PlaceEntity().apply {
                id = UUID.randomUUID().toString()
                name = "place"
                events += event1
            }
            insert(place)
        }
    }

    @Test
    fun `upsert one to many empty collection`() {
        with(operations) {
            val event1 = UpsertEventEntity().apply { id = UUID.randomUUID() }
            val place = PlaceEntity().apply {
                id = UUID.randomUUID().toString()
                name = "place"
                events += event1
                events.clear()
            }
            upsert(place)
        }
    }

    @Test
    fun `upsert update`() {
        with(operations) {
            val eventId = UUID.randomUUID()
            val event = UpsertEventEntity().apply {
                id = eventId
                name = "event1"
            }
            insert(event)

            val event2 = UpsertEventEntity().apply {
                id = eventId
                name = "event2"
            }
            upsert(event2)

            val events = select(UpsertEvent::class).get().toList()
            Assertions.assertThat(events).hasSize(1)
            Assertions.assertThat(events!!.first().name).isEqualTo(event2.name)
        }
    }
}