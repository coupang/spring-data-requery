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
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;

/**
 * org.springframework.data.requery.converters.ZonedDateTimeToStringConverter
 *
 * @author debop
 */
public class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String> {

    @Override
    public Class<ZonedDateTime> getMappedType() {
        return ZonedDateTime.class;
    }

    @Override
    public Class<String> getPersistedType() {
        return String.class;
    }

    @Nullable
    @Override
    public Integer getPersistedSize() {
        return null;
    }

    @Override
    public String convertToPersisted(ZonedDateTime value) {
        return (value != null)
               ? Java8Times.toIsoZonedDateTimeString(value)
               : null;
    }

    @Override
    public ZonedDateTime convertToMapped(Class<? extends ZonedDateTime> type, @Nullable String value) {
        return (StringUtils.hasText(value))
               ? ZonedDateTime.parse(value)
               : null;
    }
}
