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

package org.springframework.data.requery.kotlin

import io.requery.query.Operator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher.matching
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.sample.UserEntity

/**
 * org.springframework.data.requery.ExampleExtensionsTest
 *
 * @author debop
 */
class ExampleExtensionsTest : AbstractDomainTest() {

    @Test
    fun `build whereclause from Example instance`() {

        val user = UserEntity().apply { lastname = "Bae" }

        val example = Example.of(user, matching().withIgnoreNullValues())

        val queryElement = example.buildQueryElement(operations, UserEntity::class)

        assertThat(queryElement).isNotNull
        assertThat(queryElement.whereElements).hasSize(1)

        // lastname = 'Bae'
        assertThat(queryElement.whereElements.find { it.condition.operator == Operator.EQUAL }).isNotNull
    }
}