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

package org.springframework.data.requery.kotlin

import io.requery.query.Limit
import io.requery.query.Offset
import io.requery.query.Order
import io.requery.query.OrderingExpression
import io.requery.query.element.QueryElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.sample.User

/**
 * org.springframework.data.requery.QueryElementExtensionsTest
 *
 * @author debop
 */
@SuppressWarnings("UNCHECKED_CAST")
class QueryElementExtensionsTest : AbstractDomainTest() {

    @Test
    fun `apply sort to QueryElement`() {

        val sort = Sort.by(Sort.Order.desc("firstname"), Sort.Order.asc("lastname"))

        val query = kotlinDataStore.select(User::class).applySort(User::class.java, sort)

        assertThat(query.orderByExpressions).hasSize(2)
        assertThat(query.orderByExpressions.map { it.name }).containsOnly("firstname", "lastname")
        assertThat(query.orderByExpressions.map { it as OrderingExpression<*> }.map { it.order }).containsExactly(Order.DESC, Order.ASC)
    }

    @Test
    fun `apply pageable to QueryElement`() {

        val pageable = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "firstname", "lastname"))

        val query = kotlinDataStore.select(User::class).applyPageable(User::class.java, pageable)

        assertThat(query).isInstanceOf(QueryElement::class.java)
        assertThat(query).isInstanceOf(Limit::class.java)
        assertThat(query).isInstanceOf(Offset::class.java)

        assertThat(query.offset).isEqualTo(pageable.offset)
        assertThat(query.limit).isEqualTo(pageable.pageSize)


        assertThat(query.orderByExpressions).hasSize(2)
        assertThat(query.orderByExpressions.map { it.name }).containsOnly("firstname", "lastname")
        assertThat(query.orderByExpressions.map { it as OrderingExpression<*> }.map { it.order }).containsOnly(Order.DESC)
    }

    @Test
    fun `get OrderingExpression from Entity`() {

        val sort = Sort.by(Sort.Order.desc("firstname"), Sort.Order.asc("lastname"))

        val orderExprs = User::class.java.getOrderingExpressions(sort)

        assertThat(orderExprs).hasSize(2)
        assertThat(orderExprs[0].name).isEqualTo("firstname")
        assertThat(orderExprs[0].order).isEqualTo(Order.DESC)

        assertThat(orderExprs[1].name).isEqualTo("lastname")
        assertThat(orderExprs[1].order).isEqualTo(Order.ASC)
    }
}