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

package org.springframework.data.requery.domain.sample;

import io.requery.query.Condition;
import io.requery.query.Expression;
import io.requery.query.NamedExpression;
import io.requery.query.Result;
import io.requery.query.element.QueryElement;
import lombok.experimental.UtilityClass;
import org.springframework.data.requery.core.RequeryOperations;

import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * Collection of {@link QueryElement}s for a {@link User}
 *
 * @author debop
 * @since 18. 6. 25
 */
@UtilityClass
@SuppressWarnings("unchecked")
public class UserQueryElements {

    public static QueryElement<? extends Result<User>> userHasFirstname(RequeryOperations operations, final String firstname) {
        return simplePropertyQueryElement(operations, "firstname", firstname);
    }

    public static QueryElement<? extends Result<User>> userHasLastname(RequeryOperations operations, final String lastname) {
        return simplePropertyQueryElement(operations, "lastname", lastname);
    }

    public static QueryElement<? extends Result<User>> userHasFirstnameLike(RequeryOperations operations,
                                                                            final String expression) {
        Condition<?, ?> filter = NamedExpression.ofString("firstname").like("%" + expression + "%");

        return (QueryElement<? extends Result<User>>) unwrap(
            operations
                .select(User.class)
                .where(filter)
        );
    }

    public static QueryElement<? extends Result<User>> userHasLastnameLikeWithSort(RequeryOperations operations,
                                                                                   final String expression) {
        Condition<?, ?> expr = NamedExpression.ofString("lastname").like("%" + expression + "%");
        Expression<String> firstNameSortExpr = NamedExpression.ofString("firstname").asc();

        return (QueryElement<? extends Result<User>>) unwrap(
            operations
                .select(User.class)
                .where(expr)
                .orderBy(firstNameSortExpr)
        );
    }

    @SuppressWarnings("unchecked")
    private QueryElement<? extends Result<User>> simplePropertyQueryElement(RequeryOperations operations,
                                                                            String propertyName,
                                                                            final String propertyValue) {
        Condition<?, ?> filter = NamedExpression.ofString(propertyName).eq(propertyValue);

        return (QueryElement<? extends Result<User>>) unwrap(
            operations
                .select(User.class)
                .where(filter)
        );

    }
}
