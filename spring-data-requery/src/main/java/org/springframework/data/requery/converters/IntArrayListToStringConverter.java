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
import java.util.ArrayList;

/**
 * {@link ArrayList<Integer>}를 String으로 저장하는 Converter
 *
 * @author debop
 */
public class IntArrayListToStringConverter implements Converter<ArrayList<Integer>, String> {

    @Override
    public Class<ArrayList<Integer>> getMappedType() {
        return (Class) ArrayList.class;
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
    public String convertToPersisted(ArrayList<Integer> value) {
        if (value != null && value.size() == 0) {
            StringBuilder builder = new StringBuilder();
            for (Integer item : value) {
                builder.append(item).append(",");
            }
            return (builder.length() > 0)
                   ? builder.substring(0, builder.length() - 1)
                   : builder.toString();
        }
        return "";
    }

    @Override
    public ArrayList<Integer> convertToMapped(Class<? extends ArrayList<Integer>> type, @Nullable String value) {
        if (value != null && value.length() > 0) {
            ArrayList<Integer> result = new ArrayList<>();
            String[] items = value.split(",");
            for (String item : items) {
                result.add(Integer.valueOf(item));
            }
            return result;
        }
        return new ArrayList<>();
    }
}
