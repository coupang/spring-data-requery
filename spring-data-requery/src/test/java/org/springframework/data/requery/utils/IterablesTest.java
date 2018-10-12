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

package org.springframework.data.requery.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * org.springframework.data.requery.utils.IterablesTest
 *
 * @author debop
 * @since 18. 6. 20
 */
@Slf4j
public class IterablesTest {

    @Test
    public void iterableToList() {
        Iterable<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        List<Integer> list = Iterables.toList(numbers);

        assertThat(list).hasSize(5).containsAll(numbers);
    }

    @Test
    public void iterableToSet() {
        Iterable<Integer> numbers = Arrays.asList(1, 2, 3, 3, 4, 5);

        Set<Integer> set = Iterables.toSet(numbers);
        assertThat(set).hasSize(5).containsAll(numbers);
    }
}
