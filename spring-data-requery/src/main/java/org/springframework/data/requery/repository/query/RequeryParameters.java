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

package org.springframework.data.requery.repository.query;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 메소드의 Parameter 정보를 나타냅니다.
 *
 * @author debop
 * @since 18. 6. 7
 */
@Slf4j
public class RequeryParameters extends Parameters<RequeryParameters, RequeryParameters.RequeryParameter> {

    public RequeryParameters(@NotNull final Method method) {
        super(method);
        log.debug("Ctor RequeryParameters. method={}", method);
    }

    public RequeryParameters(@NotNull final List<RequeryParameter> parameters) {
        super(parameters);
        log.debug("Ctor RequeryParameters. parameters={}", parameters);
    }

    @NotNull
    @Override
    protected RequeryParameter createParameter(@NotNull final MethodParameter parameter) {
        return new RequeryParameter(parameter);
    }

    @NotNull
    @Override
    protected RequeryParameters createFrom(@NotNull final List<RequeryParameter> parameters) {
        return new RequeryParameters(parameters);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        getBindableParameters().forEach(param -> {
            builder.append(param.toString()).append(",");
        });

        return "(" + (builder.length() > 0 ? builder.substring(0, builder.length() - 1) : "") + ")";
    }

    @Slf4j
    static class RequeryParameter extends Parameter {

        @NotNull
        private final MethodParameter parameter;

        /**
         * Creates a new {@link Parameter} for the given {@link MethodParameter}.
         *
         * @param parameter must not be {@literal null}.
         */
        public RequeryParameter(@NotNull final MethodParameter parameter) {
            super(parameter);
            this.parameter = parameter;

            log.debug("Create RequeryParameter. parameter={}", parameter);
        }

        public boolean isDateParameter() {
            return Objects.equals(getType(), Date.class);
        }

        @NotNull
        @Override
        public String toString() {
            return parameter.getParameter().toString();
        }
    }
}
