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

import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.ExampleMatcher.matching;

public class ExampleTest {

    Person person;
    Example<Person> example;

    @Before
    public void setup() {
        person = new Person();
        person.setFirstname("rand");

        example = Example.of(person);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullProbe() {
        Example.of(null);
    }

    @Test
    public void returnsSampleObjectsClassAsProbeType() {
        assertThat(example.getProbeType()).isEqualTo(Person.class);
    }

    @Test
    public void shouldCompareUsingHashCodeAndEquals() throws Exception {
        Example<Person> example = Example.of(person, matching().withIgnoreCase("firstname"));
        Example<Person> sameAsExample = Example.of(person, matching().withIgnoreCase("firstname"));

        Example<Person> different = Example.of(person, matching().withMatcher("firstname",
                                                                              GenericPropertyMatchers.contains()));

        assertThat(example.hashCode()).isEqualTo(sameAsExample.hashCode());
        assertThat(example.hashCode()).isNotEqualTo(different.hashCode());
        assertThat(example).isEqualTo(sameAsExample);
        assertThat(example).isNotEqualTo(different);
    }

    @Data
    static class Person {
        String firstname;
    }
}
