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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Value Object의 속성 정보를 표현할 때 사용되는 String builder class.
 *
 * @author debop
 */
public class ToStringBuilder implements Serializable {

    @NotNull
    public static ToStringBuilder of(@NotNull final Object obj) {
        Assert.notNull(obj, "obj must not be null");
        return new ToStringBuilder(obj);
    }

    @NotNull
    public static ToStringBuilder of(@NotNull final String classname) {
        Assert.hasText(classname, "classname must has text");
        return new ToStringBuilder(classname);
    }

    public ToStringBuilder(@NotNull final Object obj) {
        this(obj.getClass().getName());
    }

    public ToStringBuilder(@NotNull final String classname) {
        Assert.hasText(classname, "classname must has text.");
        this.classname = classname;
    }

    private final String classname;
    private final transient Map<String, Object> valueMap = new HashMap<>();

    public ToStringBuilder add(@NotNull final String name, @Nullable final Object value) {
        valueMap.put(name, value != null ? value : "<null>");
        return this;
    }

    @NotNull
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return this.classname + "(" + builder.toString() + ")";
    }

    @NotNull
    public String toString(int limit) {
        return (limit > 0) ? toString().substring(0, limit) : toString();
    }

    private static final long serialVersionUID = -534567324368918410L;
}
