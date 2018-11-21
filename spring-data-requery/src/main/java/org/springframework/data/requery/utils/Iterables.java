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

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * org.springframework.data.requery.utils.Iterables
 *
 * @author debop
 * @since 18. 6. 20
 */
@UtilityClass
public class Iterables {

    @Nonnull
    public static <T> List<T> toList(@Nonnull final Iterable<T> iterable) {
        if (iterable instanceof List) {
            return (List<T>) iterable;
        } else {
            List<T> list = new ArrayList<>();
            for (T item : iterable) {
                list.add(item);
            }
            return list;
        }
    }

    @Nonnull
    public static <T> Set<T> toSet(@Nonnull Iterable<T> source) {
        if (source instanceof Set) {
            return (Set<T>) source;
        } else {
            Set<T> set = new HashSet<>();

            for (T item : source) {
                set.add(item);
            }
            return set;
        }
    }
}
