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

package org.springframework.data.requery.kotlin.domain.basic

import io.requery.Column
import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.OneToOne
import io.requery.Table
import io.requery.Transient
import java.io.Serializable


@Entity
@Table(name = "basic_address")
interface BasicLocation : io.requery.Persistable, Serializable {

    @get:Key
    @get:Generated
    val id: Long

    var line1: String
    var line2: String
    var line3: String

    @get:Column(length = 3)
    var zip: String

    @get:Column(length = 2)
    var countryCode: String

    var city: String

    var state: String

    @get:OneToOne(mappedBy = "address")
    @get:Column(name = "basic_user")
    var user: BasicUser

    @get:Transient
    var description: String

}