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

import io.requery.query.Condition;
import io.requery.query.LogicalCondition;
import io.requery.query.NamedExpression;
import io.requery.query.Result;
import io.requery.query.WhereAndOr;
import io.requery.query.element.LogicalOperator;
import io.requery.query.element.QueryElement;
import io.requery.query.function.Function;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.NullHandler;
import org.springframework.data.requery.utils.RequeryUtils;
import org.springframework.data.support.ExampleMatcherAccessor;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.ExampleMatcher.StringMatcher;
import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * Query by {@link Example} 을 수행하기 위해, Example 을 이용하여 {@link QueryElement}를 빌드합니다.
 *
 * @author debop
 * @since 18. 6. 19
 */
@Slf4j
@UtilityClass
public class QueryByExampleBuilder {

//    private static final LinkedMultiValueMap<Class<?>, Field> entityFields = new LinkedMultiValueMap<>();

    /**
     * {@link Example} 를 표현하는 {@link WhereAndOr} 조건절로 빌드합니다.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <E> QueryElement<? extends Result<E>> applyExample(@NotNull final QueryElement<? extends Result<E>> root,
                                                                     @NotNull final Example<E> example) {
        Assert.notNull(root, "Root must not be null!");
        Assert.notNull(example, "Example must not be null!");

        ExampleMatcher matcher = example.getMatcher();

        List<Condition<E, ?>> conditions = getConditions(root,
                                                         example.getProbe(),
                                                         example.getProbeType(),
                                                         new ExampleMatcherAccessor(matcher));

        LogicalCondition<E, ?> whereCondition = null;
        if (matcher.isAllMatching()) {
            whereCondition = RequeryUtils.foldConditions(conditions, LogicalOperator.AND);
        } else if (matcher.isAnyMatching()) {
            whereCondition = RequeryUtils.foldConditions(conditions, LogicalOperator.OR);
        }

        return (whereCondition != null)
               ? (QueryElement<? extends Result<E>>) unwrap(root.where(whereCondition))
               : root;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private <E> List<Condition<E, ?>> getConditions(@NotNull final QueryElement<? extends Result<E>> root,
                                                    @NotNull final Object exampleValue,
                                                    @NotNull final Class<E> probeType,
                                                    @NotNull final ExampleMatcherAccessor exampleAccessor) {

        List<Condition<E, ?>> conditions = new ArrayList<>();
        DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(exampleValue);

        List<Field> fields = RequeryUtils.findEntityFields(probeType);

        for (Field field : fields) {
            // Query By Example을 지원하지 못하는 Field 들은 제외합니다.
            boolean notSupportedField = RequeryUtils.isAssociationField(field) ||
                                        RequeryUtils.isEmbededField(field) ||
                                        RequeryUtils.isTransientField(field);

            if (notSupportedField || exampleAccessor.isIgnoredPath(field.getName())) {
                continue;
            }

            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            Object fieldValue = beanWrapper.getPropertyValue(fieldName);

            log.trace("Get condition from Example. filed={}, fieldValue={}", field, fieldValue);

            NamedExpression<?> expr = NamedExpression.of(fieldName, fieldType);

            if (fieldValue == null) {
                if (exampleAccessor.getNullHandler().equals(NullHandler.INCLUDE)) {
                    conditions.add((Condition<E, ?>) expr.isNull());
                }
            } else if (fieldType.equals(String.class)) {
                Condition<E, ?> condition = buildStringCondition(exampleAccessor,
                                                                 (NamedExpression<String>) expr,
                                                                 fieldName,
                                                                 (String) fieldValue);
                conditions.add(condition);

            } else {
                Condition<E, ?> condition = (Condition<E, ?>) ((NamedExpression<Object>) expr).eq(fieldValue);
                conditions.add(condition);
            }
        }

        return conditions;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private <E> Condition<E, ?> buildStringCondition(@NotNull final ExampleMatcherAccessor exampleAccessor,
                                                     @NotNull final NamedExpression<String> expression,
                                                     @NotNull final String fieldName,
                                                     @NotNull final String fieldValue) {
        Boolean ignoreCase = exampleAccessor.isIgnoreCaseForPath(fieldName);

        log.trace("Matching with ignoreCase? ignoreCase={}", ignoreCase);

        StringMatcher matcher = exampleAccessor.getStringMatcherForPath(fieldName);
        Function<String> lower = expression.function("Lower");

        switch (matcher) {
            case DEFAULT:
            case EXACT:
                return (Condition<E, ?>) (ignoreCase
                                          ? lower.eq(fieldValue.toLowerCase())
                                          : expression.eq(fieldValue));
            case CONTAINING:
                return (Condition<E, ?>) (ignoreCase
                                          ? lower.like("%" + fieldValue.toLowerCase() + "%")
                                          : expression.like("%" + fieldValue + "%"));
            case STARTING:
                return (Condition<E, ?>) (ignoreCase
                                          ? lower.like((fieldValue + "%").toLowerCase())
                                          : expression.like(fieldValue + "%"));

            case ENDING:
                return (Condition<E, ?>) (ignoreCase
                                          ? lower.like(("%" + fieldValue).toLowerCase())
                                          : expression.like("%" + fieldValue));
            default:
                throw new IllegalArgumentException("Unsupported StringMatcher " + matcher);
        }
    }
}

