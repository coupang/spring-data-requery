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
import java.time.LocalTime;

/**
 * {@link LocalTime} 을 {@link Long} (Timestamp) 로 저장하는 Converter
 *
 * @author debop
 */
public class LocalTimeToLongConverter implements Converter<LocalTime, Long> {

    @Override
    public Class<LocalTime> getMappedType() {
        return LocalTime.class;
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

    @Nullable
    @Override
    public Long convertToPersisted(@Nullable LocalTime value) {
        return (value != null) ? value.toNanoOfDay() : null;
    }

    @Nullable
    @Override
    public LocalTime convertToMapped(Class<? extends LocalTime> type, @Nullable Long value) {
        return (value != null) ? LocalTime.ofNanoOfDay(value) : null;
    }
}
