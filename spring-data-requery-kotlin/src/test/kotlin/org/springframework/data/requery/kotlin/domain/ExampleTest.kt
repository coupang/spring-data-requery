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

package org.springframework.data.requery.kotlin.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers
import org.springframework.data.domain.ExampleMatcher.matching

/**
 * org.springframework.data.requery.kotlin.domain.ExampleTest
 *
 * @author debop
 */
class ExampleTest {

    private data class Person(val firstname: String)

    private lateinit var person: Person
    private lateinit var example: Example<Person>

    @Before
    fun setup() {
        person = Person("rand")
        example = Example.of(person)
    }

    @Test
    fun `returns sample objects class as probetype`() {

        assertThat(example.probeType).isEqualTo(Person::class.java)

    }

    @Test
    fun `should compare using hashCode and equals`() {
        val example = Example.of(person, matching().withIgnoreCase("firstname"))
        val sameAsExample = Example.of(person, matching().withIgnoreCase("firstname"))

        val different = Example.of(person, matching().withMatcher("firstname", GenericPropertyMatchers.contains()))

        assertThat(example.hashCode()).isEqualTo(sameAsExample.hashCode())
        assertThat(example.hashCode()).isNotEqualTo(different.hashCode())

        assertThat(example).isEqualTo(sameAsExample)
        assertThat(example).isNotEqualTo(different)
    }
}