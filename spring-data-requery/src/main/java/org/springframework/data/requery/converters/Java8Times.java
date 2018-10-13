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

package org.springframework.data.requery.converters;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * java times (JDK 8 Times) 용 Utility class 입니다.
 *
 * @author debop
 */
@UtilityClass
public class Java8Times {

    public static final long NANO_IN_SECONDS = 1_000_000_000L;

    @NotNull
    public static String toIsoOffsetDateTimeString(@NotNull TemporalAccessor accessor) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(accessor);
    }

    @NotNull
    public static String toIsoOffsetDateString(@NotNull TemporalAccessor accessor) {
        return DateTimeFormatter.ISO_OFFSET_DATE.format(accessor);
    }

    @NotNull
    public static String toIsoOffsetTimeString(@NotNull TemporalAccessor accessor) {
        return DateTimeFormatter.ISO_OFFSET_TIME.format(accessor);
    }

    @NotNull
    public static String toIsoZonedDateTimeString(@NotNull TemporalAccessor accessor) {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(accessor);
    }
}
