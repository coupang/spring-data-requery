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

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import io.requery.Table
import mu.KotlinLogging
import java.io.Serializable

@Entity
@Table(name = "basic_keyword")
interface Keywords : Persistable, Serializable {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    @get:Key
    @get:Generated
    val id: Int

    var isNotJvmKeywords: String

    var isNewKeyword: Boolean
    var isDefaultKeyword: Boolean

    var getAbstract: String

}