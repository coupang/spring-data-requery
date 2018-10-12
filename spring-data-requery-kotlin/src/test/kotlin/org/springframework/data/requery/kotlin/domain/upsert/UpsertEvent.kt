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

import com.fasterxml.jackson.annotation.JsonProperty
import io.requery.CascadeAction
import io.requery.Entity
import io.requery.Key
import io.requery.ManyToMany
import io.requery.ManyToOne
import io.requery.Persistable
import io.requery.Table
import java.util.*

@Entity
@Table(name = "UpsertEvent")
interface UpsertEvent : Persistable {

    @get:Key
    //    @get:JsonProperty("_id")
    var id: UUID

    @get:JsonProperty("_name")
    var name: String?

    @get:JsonProperty("_place")
    @get:ManyToOne(cascade = [CascadeAction.SAVE, CascadeAction.DELETE])
    var place: Place?

    @get:JsonProperty("_tags")
    @get:ManyToMany(cascade = [CascadeAction.SAVE, CascadeAction.DELETE])
    val tags: MutableSet<Tag>
}