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

/**
 * org.springframework.data.requery.converters.IntArrayToStringConverter
 *
 * @author debop
 */
public class IntArrayToStringConverter implements Converter<int[], String> {
    @Override
    public Class<int[]> getMappedType() {
        return int[].class;
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
    public String convertToPersisted(int[] value) {
        if (value != null && value.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int item : value) {
                builder.append(item).append(",");
            }
            return (builder.length() > 0)
                   ? builder.substring(0, builder.length() - 1)
                   : builder.toString();
        }
        return null;
    }

    @Override
    public int[] convertToMapped(Class<? extends int[]> type, @Nullable String value) {
        if (value != null && value.length() > 0) {
            String[] items = value.split(",");
            int[] result = new int[items.length];

            for (int i = 0; i < items.length; i++) {
                result[i] = Integer.valueOf(items[i]);
            }
            return result;
        }

        return null;
    }
}
