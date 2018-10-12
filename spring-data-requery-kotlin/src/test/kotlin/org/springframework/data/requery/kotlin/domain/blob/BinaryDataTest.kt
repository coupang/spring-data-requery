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

package org.springframework.data.requery.kotlin.domain.blob

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest

/**
 * org.springframework.data.requery.kotlin.domain.blob.BinaryDataTest
 *
 * @author debop
 */
class BinaryDataTest : AbstractDomainTest() {

    @Test
    fun `save byte array data`() {

        val bytes = ByteArray(8192)
        rnd.nextBytes(bytes)

        val binData = BinaryDataEntity().apply {
            name = "binary data"
            picture = bytes
        }

        operations.insert(binData)
        assertThat(binData.id).isNotNull()

        val saved = operations.findById(BinaryData::class, binData.id)!!
        assertThat(saved.id).isEqualTo(binData.id)
        assertThat(saved.name).isEqualTo(binData.name)
        assertThat(saved.picture).isNotNull().isEqualTo(binData.picture)

        rnd.nextBytes(bytes)
        saved.picture = bytes
        operations.upsert(saved)
    }
}