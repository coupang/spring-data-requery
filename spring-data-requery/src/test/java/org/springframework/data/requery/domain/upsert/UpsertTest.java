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

package org.springframework.data.requery.domain.upsert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Diego on 2018. 6. 10..
 */
public class UpsertTest extends AbstractDomainTest {

    @Before
    public void setup() {
        requeryOperations.deleteAll(UpsertTag.class);
        requeryOperations.deleteAll(UpsertEvent.class);
        requeryOperations.deleteAll(UpsertPlace.class);
        requeryOperations.deleteAll(UpsertLocation.class);
    }

    @Test
    public void insert_location_with_embedded_address() {
        UpsertLocation location = new UpsertLocation();
        location.setName("Tower 730");
        location.getAddress().setZipcode("12345");
        location.getAddress().setCity("seoul");

        requeryOperations.insert(location);

        assertThat(location.isNew()).isFalse();

        UpsertLocation loaded = requeryOperations.findById(UpsertLocation.class, location.getId());
        assertThat(loaded).isEqualTo(location);
        assertThat(loaded.getAddress().getZipcode()).isEqualTo("12345");
    }

    @Test
    public void insert_many_to_many() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("test");

        UpsertTag tag1 = new UpsertTag();
        tag1.setId(UUID.randomUUID());
        tag1.setName("tag1");
        UpsertTag tag2 = new UpsertTag();
        tag2.setId(UUID.randomUUID());
        tag2.setName("tag2");

        event.getTags().add(tag1);
        event.getTags().add(tag2);

        requeryOperations.insert(event);

        assertThat(requeryOperations.count(UpsertEvent.class).get().value()).isEqualTo(1);
        assertThat(requeryOperations.count(UpsertTag.class).get().value()).isEqualTo(2);

        UpsertEvent loaded = requeryOperations.findById(UpsertEvent.class, event.getId());

        assertThat(loaded.getTags()).hasSize(2).containsOnly(tag1, tag2);

        requeryOperations.delete(loaded);
        requeryOperations.refreshAllProperties(loaded);

        assertThat(requeryOperations.count(UpsertEvent.class).get().value()).isEqualTo(0);
        assertThat(requeryOperations.count(UpsertTag.class).get().value()).isEqualTo(0);
    }

    @Test
    public void upsert_event() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("test");

        requeryOperations.upsert(event);

        assertThat(requeryOperations.count(UpsertEvent.class).get().value()).isEqualTo(1);

        UpsertEvent loaded = requeryOperations.findById(UpsertEvent.class, event.getId());
        assertThat(loaded).isEqualTo(event);

        requeryOperations.delete(loaded);
        assertThat(requeryOperations.count(UpsertEvent.class).get().value()).isEqualTo(0);
    }

    @Test
    public void upsert_one_to_many() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("test");

        UpsertPlace place = new UpsertPlace();
        place.setId(UUID.randomUUID().toString());
        place.setName("place");

        place.getEvents().add(event);

        requeryOperations.upsert(place);
        UpsertPlace savedPlace = requeryOperations.findById(UpsertPlace.class, place.getId());

        assertThat(savedPlace.getId()).isEqualTo(place.getId());
        assertThat(savedPlace.getEvents()).hasSize(1);
        assertThat(savedPlace.getEvents().iterator().next().getId()).isEqualTo(eventId);
    }

    @Test
    public void upsert_many_to_many() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("test");

        UpsertTag tag1 = new UpsertTag();
        tag1.setId(UUID.randomUUID());
        tag1.setName("tag1");
        UpsertTag tag2 = new UpsertTag();
        tag2.setId(UUID.randomUUID());
        tag2.setName("tag2");

        event.getTags().add(tag1);
        event.getTags().add(tag2);

        requeryOperations.upsert(event);
        assertThat(requeryOperations.count(UpsertEvent.class).get().value()).isEqualTo(1);
        assertThat(requeryOperations.count(UpsertTag.class).get().value()).isEqualTo(2);

        UpsertEvent loaded = requeryOperations.findById(UpsertEvent.class, event.getId());

        requeryOperations.delete(loaded);

        assertThat(requeryOperations.count(UpsertEvent.class).get().value()).isEqualTo(0);
        assertThat(requeryOperations.count(UpsertTag.class).get().value()).isEqualTo(0);
    }

    @Test
    public void upsert_insert_one_to_many() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("test");

        UpsertPlace place = new UpsertPlace();
        place.setId(UUID.randomUUID().toString());
        place.setName("place");

        place.getEvents().add(event);

        requeryOperations.insert(place);

        UpsertPlace savedPlace = requeryOperations.findById(UpsertPlace.class, place.getId());

        assertThat(savedPlace.getId()).isEqualTo(place.getId());
        assertThat(savedPlace.getEvents()).hasSize(1);
        assertThat(savedPlace.getEvents().iterator().next().getId()).isEqualTo(eventId);
    }

    @Test
    public void upsert_one_to_many_with_empty_collection() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("test");

        UpsertPlace place = new UpsertPlace();
        place.setId(UUID.randomUUID().toString());

        place.getEvents().add(event);
        place.getEvents().remove(event);

        requeryOperations.upsert(place);
        UpsertPlace savedPlace = requeryOperations.findById(UpsertPlace.class, place.getId());

        assertThat(savedPlace.getId()).isEqualTo(place.getId());
        assertThat(savedPlace.getEvents()).hasSize(0);
    }

    @Test
    public void upsert_exists_entity() {
        UUID eventId = UUID.randomUUID();
        UpsertEvent event = new UpsertEvent();
        event.setId(eventId);
        event.setName("event1");

        requeryOperations.insert(event);

        UpsertEvent event2 = new UpsertEvent();
        event2.setId(eventId);
        event2.setName("event2");

        requeryOperations.upsert(event2);

        Iterable<UpsertEvent> events = requeryOperations.findAll(UpsertEvent.class);
        assertThat(events).hasSize(1).containsOnly(event2);
    }

}
