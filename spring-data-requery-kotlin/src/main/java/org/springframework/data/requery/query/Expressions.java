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

package org.springframework.data.requery.query;

import io.requery.query.Expression;
import io.requery.query.FieldExpression;
import io.requery.query.LogicalCondition;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Expressions
 * <p>
 * NOTE: Java API 를 Kotlin 에서 호출 시에 ambiguous type error 가 발생할 때가 있다.
 * NOTE: 이 때에는 원하는 메소드만 Java Code 로 구현해서, Kotlin에서 호출하면 됩니다.
 *
 * @author debop
 */
public final class Expressions {

    private Expressions() {}

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <V> LogicalCondition<? extends Expression<V>, Collection<V>> in(@Nonnull final FieldExpression<V> expression,
                                                                                  @Nonnull final Collection<V> values) {
        return expression.in(values);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <V> LogicalCondition<? extends Expression<V>, Collection<V>> notIn(@Nonnull final FieldExpression<V> expression,
                                                                                     @Nonnull final Collection<V> values) {
        return expression.notIn(values);
    }
}
