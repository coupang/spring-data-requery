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

package org.springframework.data.requery.domain;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.ExampleMatcher.NullHandler;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.ExampleMatcher.matching;
import static org.springframework.data.domain.ExampleMatcher.matchingAll;
import static org.springframework.data.domain.ExampleMatcher.matchingAny;

public class ExampleMatcherTest {

    ExampleMatcher matcher;

    @Before
    public void setup() {
        matcher = matching();
    }

    @Test
    public void defaultStringMatcherShouldReturnDefault() {
        assertThat(matcher.getDefaultStringMatcher()).isEqualTo(StringMatcher.DEFAULT);
    }

    @Test
    public void ignoreCaseShouldReturnFalseByDefault() {
        assertThat(matcher.isIgnoreCaseEnabled()).isFalse();
    }

    @Test
    public void ignoredPathsIsEmptyByDefault() {
        assertThat(matcher.getIgnoredPaths()).isEmpty();
    }

    @Test
    public void nullHandlerShouldReturnIgnoreByDefault() {
        assertThat(matcher.getNullHandler()).isEqualTo(NullHandler.IGNORE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void ignoredPathsIsNotMidifiable() {
        matcher.getIgnoredPaths().add("-\\_(가)_/-");
    }

    @Test
    public void ignoredCaseShouldReturnTrueWhenIgnoreCaseEnabled() {
        matcher = matching().withIgnoreCase();
        assertThat(matcher.isIgnoreCaseEnabled()).isTrue();
    }

    @Test
    public void nullHandlerShouldReturnInclude() {
        matcher = matching().withIncludeNullValues();
        assertThat(matcher.getNullHandler()).isEqualTo(NullHandler.INCLUDE);
    }

    @Test
    public void nullHandlerShouldReturnConfiguredValue() {
        matcher = matching().withNullHandler(NullHandler.INCLUDE);
        assertThat(matcher.getNullHandler()).isEqualTo(NullHandler.INCLUDE);
    }

    @Test
    public void ignoredPathsShouldReturnCorrectProperties() {
        matcher = matching().withIgnorePaths("foo", "bar", "baz");

        assertThat(matcher.getIgnoredPaths())
            .hasSize(3)
            .containsOnly("foo", "bar", "baz");
    }

    @Test
    public void ignoredPathsShouldReturnUniqueProperties() {
        matcher = matching().withIgnorePaths("foo", "bar", "foo");

        assertThat(matcher.getIgnoredPaths())
            .hasSize(2)
            .containsOnly("foo", "bar");
    }

    @Test
    public void withCreatesNewInstance() {
        matcher = matching().withIgnorePaths("foo", "bar", "foo");

        ExampleMatcher configuredExampleSpec = matcher.withIgnoreCase();

        assertThat(matcher).isNotEqualTo(configuredExampleSpec);
        assertThat(matcher.getIgnoredPaths()).hasSize(2);
        assertThat(matcher.isIgnoreCaseEnabled()).isFalse();

        assertThat(configuredExampleSpec.getIgnoredPaths()).hasSize(2);
        assertThat(configuredExampleSpec.isIgnoreCaseEnabled()).isTrue();
    }

    @Test
    public void defaultMatcherRequiresAllMatching() {
        assertThat(matching().isAllMatching()).isTrue();
        assertThat(matching().isAnyMatching()).isFalse();
    }

    @Test
    public void allMatcherYieldsAllMatching() {
        assertThat(matchingAll().isAllMatching()).isTrue();
        assertThat(matchingAll().isAnyMatching()).isFalse();
    }

    @Test
    public void anyMatcherYieldsAnyMatching() {
        assertThat(matchingAny().isAnyMatching()).isTrue();
        assertThat(matchingAny().isAllMatching()).isFalse();
    }

    @Test
    public void shouldCompareUsingHashCodeAndEquals() {

        matcher = matching()
            .withIgnorePaths("foo", "bar", "baz")
            .withNullHandler(NullHandler.IGNORE)
            .withIgnoreCase("ignored-case")
            .withMatcher("hello", GenericPropertyMatchers.contains().caseSensitive())
            .withMatcher("world", matcher -> matcher.endsWith());

        ExampleMatcher sameAsMatcher = matching()
            .withIgnorePaths("foo", "bar", "baz")
            .withNullHandler(NullHandler.IGNORE)
            .withIgnoreCase("ignored-case")
            .withMatcher("hello", GenericPropertyMatchers.contains().caseSensitive())
            .withMatcher("world", matcher -> matcher.endsWith());

        ExampleMatcher different = matching()
            .withIgnorePaths("foo", "bar", "baz")
            .withNullHandler(NullHandler.IGNORE)
            .withMatcher("hello", GenericPropertyMatchers.contains().ignoreCase());

        assertThat(matcher.hashCode()).isEqualTo(sameAsMatcher.hashCode());
        assertThat(matcher).isEqualTo(sameAsMatcher);

        assertThat(matcher.hashCode()).isNotEqualTo(different.hashCode());
        assertThat(matcher).isNotEqualTo(different);
    }
}
