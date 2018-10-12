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
import io.requery.ForeignKey
import io.requery.Generated
import io.requery.Key
import io.requery.ManyToMany
import io.requery.OneToOne
import io.requery.Persistable
import io.requery.Table
import java.io.Serializable
import java.net.URL
import java.time.LocalDate
import java.util.*

/**
 * NOTE: kotlin 언어로 requery를 사용하려면 entity는 interface로 정의해야 합니다.
 */
@Entity
@Table(name = "basic_user")
interface BasicUser : Persistable, Serializable {

    @get:Key
    @get:Generated
    val id: Int

    var name: String
    var email: String
    var birthday: LocalDate
    var age: Int?

    @get:ForeignKey
    @get:OneToOne
    var address: BasicLocation

    @get:ManyToMany(mappedBy = "members")
    var groups: Set<BasicGroup>

    var about: String?

    @get:Column(unique = true)
    var uuid: UUID

    var homepage: URL?

    var picture: String?
}