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

import io.requery.Converter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * org.springframework.data.requery.converters.LocalDateTimeToLongConverter
 *
 * @author debop
 */
public class LocalDateTimeToLongConverter implements Converter<LocalDateTime, Long> {


    @Override
    public Class<LocalDateTime> getMappedType() {
        return LocalDateTime.class;
    }

    @Override
    public Class<Long> getPersistedType() {
        return Long.class;
    }

    @Nullable
    @Override
    public Integer getPersistedSize() {
        return null;
    }

    @Override
    public Long convertToPersisted(LocalDateTime value) {
        return (value != null)
               ? value.toEpochSecond(ZoneOffset.UTC) * Java8Times.NANO_IN_SECONDS + value.getNano()
               : null;
    }

    @Override
    public LocalDateTime convertToMapped(Class<? extends LocalDateTime> type, @Nullable Long value) {
        if (value != null) {
            long epochSecond = value / Java8Times.NANO_IN_SECONDS;
            int nano = (int) (value % Java8Times.NANO_IN_SECONDS);

            return LocalDateTime.ofEpochSecond(epochSecond, nano, ZoneOffset.UTC);
        }

        return null;
    }
}
