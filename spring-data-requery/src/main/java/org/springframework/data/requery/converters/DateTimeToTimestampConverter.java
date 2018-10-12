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
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.sql.Timestamp;

/**
 * Convert joda-time {@link DateTime} to {@link Timestamp}
 *
 * @author debop
 */
public class DateTimeToTimestampConverter implements Converter<DateTime, Timestamp> {

    @Override
    public Class<DateTime> getMappedType() {
        return DateTime.class;
    }

    @Override
    public Class<Timestamp> getPersistedType() {
        return Timestamp.class;
    }

    @Nullable
    @Override
    public Integer getPersistedSize() {
        return null;
    }

    @Override
    public Timestamp convertToPersisted(DateTime value) {
        return (value != null) ? new Timestamp(value.getMillis()) : null;
    }

    @Override
    public DateTime convertToMapped(Class<? extends DateTime> type, @Nullable Timestamp value) {
        return (value != null) ? new DateTime(value.getTime()) : null;
    }
}
