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
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers
import org.springframework.data.domain.ExampleMatcher.NullHandler
import org.springframework.data.domain.ExampleMatcher.StringMatcher
import org.springframework.data.domain.ExampleMatcher.matching
import org.springframework.data.domain.ExampleMatcher.matchingAll
import org.springframework.data.domain.ExampleMatcher.matchingAny

/**
 * org.springframework.data.requery.kotlin.domain.ExampleMatcherTest
 *
 * @author debop
 */
class ExampleMatcherTest {

    private lateinit var matcher: ExampleMatcher

    @Before
    fun setup() {
        matcher = matching()
    }

    @Test
    fun `default StringMatcher should return DEFAULT`() {
        assertThat(matcher.defaultStringMatcher).isEqualTo(StringMatcher.DEFAULT)
    }

    @Test
    fun `ignoreCase should return False by default`() {
        assertThat(matcher.isIgnoreCaseEnabled).isFalse()
    }

    @Test
    fun `ingored paths is emtpy by default`() {
        assertThat(matcher.ignoredPaths).isEmpty()
    }

    @Test
    fun `null handler should return IGNORE by default`() {
        assertThat(matcher.nullHandler).isEqualTo(NullHandler.IGNORE)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `ignored paths is not modifiable`() {
        matcher.ignoredPaths.add("-\\_(가)_/-")
    }

    @Test
    fun `ignoredCase should return True when ignoredCaseEnabled`() {
        matcher = matching().withIgnoreCase()
        assertThat(matcher.isIgnoreCaseEnabled).isTrue()
    }

    @Test
    fun `nullHandler should return INCLUDE`() {
        matcher = matching().withIncludeNullValues()
        assertThat(matcher.nullHandler).isEqualTo(NullHandler.INCLUDE)
    }

    @Test
    fun `nullHandler should return configured value`() {
        matcher = matching().withNullHandler(NullHandler.IGNORE)
        assertThat(matcher.nullHandler).isEqualTo(NullHandler.IGNORE)
    }

    @Test
    fun `ignoredPaths should return correct properties`() {
        matcher = matching().withIgnorePaths("foo", "bar", "baz")

        assertThat(matcher.ignoredPaths)
            .hasSize(3)
            .containsOnly("foo", "bar", "baz")
    }

    @Test
    fun `ignoredPaths should return unique properties`() {
        matcher = matching().withIgnorePaths("foo", "bar", "FOO", "foo")

        assertThat(matcher.ignoredPaths)
            .hasSize(3)
            .containsOnly("foo", "bar", "FOO")
    }

    @Test
    fun `with creates new instance`() {
        matcher = matching().withIgnorePaths("foo", "bar", "foo")

        val configuredExampleSpec = matcher.withIgnoreCase()

        assertThat(matcher).isNotEqualTo(configuredExampleSpec)
        assertThat(matcher.ignoredPaths).hasSize(2)
        assertThat(matcher.isIgnoreCaseEnabled).isFalse()

        assertThat(configuredExampleSpec.ignoredPaths).hasSize(2)
        assertThat(configuredExampleSpec.isIgnoreCaseEnabled).isTrue()
    }

    @Test
    fun `default matcher requires all matching`() {
        assertThat(matching().isAllMatching).isTrue()
        assertThat(matching().isAnyMatching).isFalse()
    }

    @Test
    fun `all matcher yields all matching`() {
        assertThat(matchingAll().isAllMatching).isTrue()
        assertThat(matchingAll().isAnyMatching).isFalse()
    }

    @Test
    fun `any matcher yields all matching`() {
        assertThat(matchingAny().isAllMatching).isFalse()
        assertThat(matchingAny().isAnyMatching).isTrue()
    }

    @Test
    fun `should compare using hashCode and equals`() {

        matcher = matching()
            .withIgnorePaths("foo", "bar", "baz")
            .withNullHandler(NullHandler.IGNORE)
            .withIgnoreCase("ignored-case")
            .withMatcher("hello", GenericPropertyMatchers.contains().caseSensitive())
            .withMatcher("world") { matcher -> matcher.endsWith() }

        val sameAsMatcher = matching()
            .withIgnorePaths("foo", "bar", "baz")
            .withNullHandler(NullHandler.IGNORE)
            .withIgnoreCase("ignored-case")
            .withMatcher("hello", GenericPropertyMatchers.contains().caseSensitive())
            .withMatcher("world") { matcher -> matcher.endsWith() }

        val different = matching()
            .withIgnorePaths("foo", "bar", "baz")
            .withNullHandler(NullHandler.IGNORE)
            .withMatcher("hello", GenericPropertyMatchers.contains().ignoreCase())

        assertThat(matcher.hashCode()).isEqualTo(sameAsMatcher.hashCode())
        assertThat(matcher).isEqualTo(sameAsMatcher)

        assertThat(matcher.hashCode()).isNotEqualTo(different.hashCode())
        assertThat(matcher).isNotEqualTo(different)
    }
}