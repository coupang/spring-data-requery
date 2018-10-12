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

@file:JvmName("QueryElementExtensions")

package org.springframework.data.requery.kotlin

import io.requery.query.Condition
import io.requery.query.LogicalCondition
import io.requery.query.NamedExpression
import io.requery.query.OrderingExpression
import io.requery.query.Result
import io.requery.query.Return
import io.requery.query.Scalar
import io.requery.query.element.LogicalOperator
import io.requery.query.element.QueryElement
import io.requery.query.element.QueryWrapper
import io.requery.query.element.WhereConditionElement
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

private val log = KotlinLogging.logger { }

fun <V : Any> namedExpressionOf(name: String, type: Class<V>): NamedExpression<V> =
    NamedExpression.of(name, type)

@Suppress("UNCHECKED_CAST")
fun <T : Any> Return<T>.unwrap(): QueryElement<T> {
    return if(this is QueryWrapper<*>) {
        this.unwrapQuery() as QueryElement<T>
    } else {
        this as QueryElement<T>
    }
}

fun <T : Any> Return<T>.unwrapAny(): QueryElement<out Any> {
    return if(this is QueryWrapper<*>) {
        this.unwrapQuery() as QueryElement<out Any>
    } else {
        this as QueryElement<out Any>
    }
}

fun <T : Any> Return<T>.applyPageable(domainClass: Class<out Any>, pageable: Pageable): QueryElement<T> {

    log.trace { "Apply paging .. domainClass=${domainClass.simpleName}, pageable=$pageable" }

    var query = this.unwrap()

    if(pageable.isUnpaged) {
        return query
    }

    if(pageable.sort.isSorted) {
        query = query.applySort(domainClass, pageable.sort)
    }

    if(pageable.pageSize > 0 && query.limit == null) {
        query = query.limit(pageable.pageSize).unwrap()
    }
    if(pageable.offset > 0 && query.offset == null) {
        query = query.offset(pageable.offset.toInt()).unwrap()
    }
    return query
}

fun <T : Any> Return<T>.applySort(domainClass: Class<out Any>, sort: Sort): QueryElement<T> {

    log.trace { "Apply sort, domainClass=${domainClass.simpleName}, sort=$sort" }

    var query = this.unwrap()

    if(sort.isUnsorted) {
        return query
    }

    sort.forEach { order ->

        val propertyName = order.property
        val direction = order.direction

        // 이미 있을 수 있다
        val orderExpr = query.orderByExpressions?.find { it.name == propertyName }

        if(orderExpr == null) {
            domainClass.getExpression(propertyName)?.let { expr ->
                query = query.orderBy(if(direction.isAscending) expr.asc() else expr.desc()).unwrap()
            }
        }
    }

    return query
}


fun <T : Any> Return<T>.applyWhereConditions(conditionElements: Set<WhereConditionElement<out Any>>): QueryElement<T> {

    val query = this.unwrap()

    if(conditionElements.isEmpty()) {
        return query
    }

    if(conditionElements.size == 1) {
        return query.where(conditionElements.first().condition).unwrap()
    }

    var whereElement = query.where(conditionElements.first().condition)

    conditionElements
        .drop(1)
        .forEach { conditionElement ->
            val condition = conditionElement.condition
            val operator = conditionElement.operator

            log.trace { "Apply where condition=$condition, operator=$operator" }

            operator?.let {
                whereElement = when(it) {
                    LogicalOperator.AND -> whereElement.and(condition)
                    LogicalOperator.OR -> whereElement.or(condition)
                    LogicalOperator.NOT -> whereElement.and(condition).not()
                }
            }
        }

    return whereElement.unwrap()
}

@Suppress("UNCHECKED_CAST")
fun Return<out Any>.getAsResult(): Result<out Any> = this.get() as Result<out Any>

@Suppress("UNCHECKED_CAST")
fun <E : Any> Return<out Any>.getAsResultEntity(): Result<E> = this.get() as Result<E>

@Suppress("UNCHECKED_CAST")
fun Return<out Any>.getAsScalarInt(): Scalar<Int> = this.get() as Scalar<Int>

fun Class<out Any>.getOrderingExpressions(sort: Sort): Array<OrderingExpression<out Any>> {

    if(sort.isUnsorted) {
        return emptyArray()
    }

    return sort.mapNotNull { order ->
        val propertyName = order.property

        this.getExpression(propertyName)?.let { expr ->
            when(order.direction) {
                Sort.Direction.ASC -> expr.asc()
                else -> expr.desc()
            }
        }
    }.toTypedArray()
}

@Suppress("UNCHECKED_CAST")
fun <E : Any> Iterable<Condition<E, *>>.foldConditions(): LogicalCondition<E, *>? {

    var result: LogicalCondition<E, *>? = null

    this.forEach { cond ->
        log.trace { "Fold conditions. cond=$cond" }
        result = when(result) {
            null -> cond as? LogicalCondition<E, *>
            else -> result!!.and(cond)
        } as LogicalCondition<E, *>
    }

    return result
}

@Suppress("UNCHECKED_CAST")
fun <E : Any> Iterable<Condition<E, *>>.foldConditions(operator: LogicalOperator): LogicalCondition<E, *>? {

    var result: LogicalCondition<E, *>? = null

    this.forEach { cond ->
        log.trace { "Fold conditions. cond=${cond.leftOperand}, ${cond.operator}, ${cond.rightOperand}" }
        result = when(result) {
            null -> cond as? LogicalCondition<E, *>
            else -> when(operator) {
                LogicalOperator.AND -> result!!.and(cond)
                LogicalOperator.OR -> result!!.or(cond)
                LogicalOperator.NOT -> result!!.and(cond).not()
            } as LogicalCondition<E, *>
        }
    }

    return result
}


