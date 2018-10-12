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

package org.springframework.data.requery.kotlin.mapping

import io.requery.Key
import io.requery.Version
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RequeryMappingContextTest {

    @Test
    fun `persistent entity rejects Spring Data @Version annotation`() {

        val context = RequeryMappingContext()
        val entity = context.getRequiredPersistentEntity(Sample::class.java)

        assertThat(entity).isNotNull

        assertThat(entity.getRequiredPersistentProperty("id").isIdProperty).isTrue()
        assertThat(entity.getRequiredPersistentProperty("springId").isIdProperty).isFalse()

        assertThat(entity.getRequiredPersistentProperty("version").isVersionProperty).isTrue()
        assertThat(entity.getRequiredPersistentProperty("springVersion").isVersionProperty).isFalse()
    }

    private class Sample {

        @get:Key
        val id: Long? = 0

        @org.springframework.data.annotation.Id
        var springId: Long = 0

        @Version
        var version: Long = 0

        @org.springframework.data.annotation.Version
        var springVersion: Long = 0
    }
}