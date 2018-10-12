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

package org.springframework.data.requery.repository.support;

import io.requery.query.NamedExpression;
import io.requery.query.OrderingExpression;
import io.requery.query.Result;
import io.requery.query.Tuple;
import io.requery.query.element.QueryElement;
import io.requery.query.function.Count;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.RandomData;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.utils.RequeryUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SimpleRequeryRepositoryQueryTest extends AbstractDomainTest {

    private final int USER_COUNT = 10;
    Set<BasicUser> users;

    @Before
    public void setup() {
        requeryTemplate.deleteAll(BasicUser.class);

        users = RandomData.randomUsers(USER_COUNT);
        requeryTemplate.insertAll(users);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findAllByIds() {

        Set<Long> ids = users
            .stream()
            .map(BasicUser::getId)
            .collect(Collectors.toSet());

        NamedExpression<Long> keyExpr = (NamedExpression<Long>) RequeryUtils.getKeyExpression(BasicUser.class);

        List<BasicUser> savedUsers = requeryTemplate
            .select(BasicUser.class)
            .where(keyExpr.in(ids))
            .get()
            .toList();

        assertThat(savedUsers).hasSize(USER_COUNT);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deleteAllByIds() {
        Set<Long> ids = users.stream().map(BasicUser::getId).collect(Collectors.toSet());

        NamedExpression<Long> keyExpr = (NamedExpression<Long>) RequeryUtils.getKeyExpression(BasicUser.class);

        Integer deletedCount = requeryTemplate
            .delete(BasicUser.class)
            .where(keyExpr.in(ids))
            .get()
            .value();

        assertThat(deletedCount).isEqualTo(USER_COUNT);
    }

    @Test
    public void findAllWithSort() {
        Sort sort = Sort.by(Sort.Order.desc("birthday"), Sort.Order.asc("name"));

        OrderingExpression<?>[] orderingExprs = RequeryUtils.getOrderingExpressions(BasicUser.class, sort);

        List<BasicUser> orderedUsers = requeryTemplate
            .select(BasicUser.class)
            .orderBy(orderingExprs)
            .get()
            .toList();

        assertThat(orderedUsers).hasSize(USER_COUNT);
        orderedUsers.forEach(user -> log.debug("user={}", user));

        for (int i = 0; i < USER_COUNT - 1; i++) {
            assertThat(orderedUsers.get(i).getBirthday().compareTo(orderedUsers.get(i + 1).getBirthday())).isGreaterThanOrEqualTo(0);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findAllWithPageableSlice() {
        Pageable pageable = PageRequest.of(1, 3);

        QueryElement<? extends Result<BasicUser>> query = (QueryElement<? extends Result<BasicUser>>)
            RequeryUtils.applyPageable(BasicUser.class,
                                       (QueryElement<? extends Result<BasicUser>>) requeryTemplate.select(BasicUser.class),
                                       pageable);

        List<BasicUser> content = query.get().toList();

        long total = requeryTemplate.count(BasicUser.class).get().value().longValue();

        Page<BasicUser> userPage = new PageImpl<>(content, pageable, total);


        assertThat(userPage.getTotalElements()).isEqualTo(USER_COUNT);
        assertThat(userPage.getContent()).hasSize(3);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void existsById() {

        Long userId = users.iterator().next().getId();
        NamedExpression<Long> keyExpr = (NamedExpression<Long>) RequeryUtils.getKeyExpression(BasicUser.class);

        Tuple result = requeryTemplate
            .select(Count.count(BasicUser.class).as("count"))
            .where(keyExpr.eq(userId))
            .get()
            .first();

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.<Integer>get("count")).isEqualTo(1);
    }
}
