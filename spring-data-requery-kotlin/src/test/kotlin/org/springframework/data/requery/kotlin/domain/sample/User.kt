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

package org.springframework.data.requery.kotlin.domain.sample

import io.requery.Column
import io.requery.Convert
import io.requery.Embedded
import io.requery.Entity
import io.requery.ForeignKey
import io.requery.Generated
import io.requery.JunctionTable
import io.requery.Key
import io.requery.ManyToMany
import io.requery.ManyToOne
import io.requery.ReferentialAction
import io.requery.Table
import org.springframework.data.requery.kotlin.converters.ByteArrayToBlobConverter
import org.springframework.data.requery.kotlin.domain.PersistableObject
import java.sql.Timestamp
import java.util.*

/**
 * org.springframework.data.requery.kotlin.domain.sample.TimedUser
 *
 * @author debop
 */
@Entity
@Table(name = "SD_User")
interface User : PersistableObject {

    @get:Key
    @get:Generated
    val id: Int?

    var firstname: String

    var lastname: String

    var age: Int?
    var active: Boolean?

    var createdAt: Timestamp?

    @get:Column(nullable = false, unique = true)
    var emailAddress: String

    @get:JunctionTable(name = "User_Colleagues", columns = [Column(name = "userId"), Column(name = "friendId")])
    @get:ManyToMany
    val colleagues: MutableSet<User>

    @get:JunctionTable
    @get:ManyToMany
    val roles: MutableSet<Role>

    @get:ForeignKey(delete = ReferentialAction.SET_NULL, update = ReferentialAction.CASCADE)
    @get:ManyToOne
    var manager: User?

    @get:Embedded
    val address: Address?

    @get:Convert(ByteArrayToBlobConverter::class)
    var binaryData: ByteArray?

    var dateOfBirth: Date?
}