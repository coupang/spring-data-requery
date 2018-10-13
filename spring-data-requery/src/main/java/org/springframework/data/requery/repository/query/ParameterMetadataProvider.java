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

import io.requery.query.Expression;
import io.requery.query.FieldExpression;
import io.requery.query.NamedExpression;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Helper class to allow easy creation of {@link ParameterMetadata}s.
 *
 * @author debop
 * @since 18. 6. 14
 */
@Slf4j
public class ParameterMetadataProvider {

    private final Iterator<? extends Parameter> parameters;
    private final List<ParameterMetadata<?>> expressions;
    private final @Nullable Iterator<Object> bindableParameterValues;

    public ParameterMetadataProvider(@NotNull final ParametersParameterAccessor accessor) {
        this(accessor.iterator(), accessor.getParameters());
    }

    public ParameterMetadataProvider(Parameters<?, ?> parameters) {
        this(null, parameters);
    }

    public ParameterMetadataProvider(@Nullable Iterator<Object> bindableParameterValues,
                                     Parameters<?, ?> parameters) {

        Assert.notNull(parameters, "Parameters must not be null!");

        this.parameters = parameters.getBindableParameters().iterator();
        this.expressions = new ArrayList<>();
        this.bindableParameterValues = bindableParameterValues;
    }

    public List<ParameterMetadata<?>> getExpressions() { return Collections.unmodifiableList(expressions); }

    @SuppressWarnings("unchecked")
    public <T> ParameterMetadata<T> next(Part part) {

        Assert.isTrue(parameters.hasNext(), "No parameter available for part. part=" + part);

        Parameter parameter = parameters.next();
        return (ParameterMetadata<T>) next(part, parameter.getType(), parameter);
    }

    @SuppressWarnings("unchecked")
    public <T> ParameterMetadata<T> next(Part part, Class<T> type) {
        Parameter parameter = parameters.next();
        Class<?> typeToUse = ClassUtils.isAssignable(type, parameter.getType()) ? parameter.getType() : type;
        return (ParameterMetadata<T>) next(part, typeToUse, parameter);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private <T> ParameterMetadata<T> next(final Part part,
                                          @NotNull final Class<T> type,
                                          final Parameter parameter) {
        log.debug("get next parameter ... part={}, type={}, parameter={}", part, type, parameter);

        Assert.notNull(type, "Type must not be null!");

        /*
         * We treat Expression types as Object vales since the real value to be bound as a parameter is determined at query time.
         */
        Class<T> reifiedType = Expression.class.equals(type) ? (Class<T>) Object.class : type;

        Supplier<String> name = () -> parameter.getName()
            .orElseThrow(() -> new IllegalArgumentException("Parameter needs to be named"));

        NamedExpression<T> expression = parameter.isExplicitlyNamed()
                                        ? NamedExpression.of(name.get(), reifiedType)
                                        : NamedExpression.of(String.valueOf(parameter.getIndex()), reifiedType);

        Object value = bindableParameterValues == null
                       ? ParameterMetadata.PLACEHOLDER
                       : bindableParameterValues.next();

        ParameterMetadata metadata = new ParameterMetadata(expression, part.getType(), value);
        expressions.add(metadata);

        return metadata;
    }


    @Slf4j
    static class ParameterMetadata<T> {

        static final Object PLACEHOLDER = new Object();

        private final FieldExpression<T> expression;
        private final Part.Type type;
        private final Object value;

        /**
         * Creates a new {@link ParameterMetadata}.
         */
        public ParameterMetadata(FieldExpression<T> expression,
                                 Part.Type type,
                                 @Nullable Object value) {

            this.expression = expression;
            this.type = value == null && Part.Type.SIMPLE_PROPERTY.equals(type) ? Part.Type.IS_NULL : type;
            this.value = value;
        }

        /**
         * Returns the {@link FieldExpression}.
         *
         * @return the expression
         */
        public FieldExpression<T> getExpression() {
            return expression;
        }

        public Object getValue() {
            return value;
        }

        /**
         * Returns whether the parameter shall be considered an {@literal IS NULL} parameter.
         */
        public boolean isIsNullParameter() {
            return Part.Type.IS_NULL.equals(type);
        }

        /**
         * Prepares the object before it's actually bound to the {@link io.requery.query.element.QueryElement}.
         *
         * @param value must not be {@literal null}.
         */
        @Nullable
        public Object prepare(Object value) {
            Assert.notNull(value, "Value must not be null!");

            Class<? extends T> expressionType = expression.getClassType();

            log.debug("Prepare value... type={}, value={}, expressionType={}", type, value, expressionType);

            if (String.class.equals(expressionType)) {

                switch (type) {
                    case STARTING_WITH:
                        return String.format("%s%%", value.toString());
                    case ENDING_WITH:
                        return String.format("%%%s", value.toString());
                    case CONTAINING:
                    case NOT_CONTAINING:
                        return String.format("%%%s%%", value.toString());
                    default:
                        return value;
                }
            }

            return value;
        }

        /**
         * Returns the given argument as {@link Collection} which means it will return it as is if it's a
         * {@link Collections}, turn an array into an {@link ArrayList} or simply wrap any other value into a single element
         * {@link Collections}.
         *
         * @param value the value to be converted to a {@link Collection}.
         * @return the object itself as a {@link Collection} or a {@link Collection} constructed from the value.
         */
        @NotNull
        private static Collection<?> toCollection(@Nullable Object value) {

            if (value == null) {
                return Collections.emptyList();
            }

            if (value instanceof Collection) {
                return (Collection<?>) value;
            }

            if (ObjectUtils.isArray(value)) {
                return Arrays.asList(ObjectUtils.toObjectArray(value));
            }

            return Collections.singleton(value);
        }
    }
}
