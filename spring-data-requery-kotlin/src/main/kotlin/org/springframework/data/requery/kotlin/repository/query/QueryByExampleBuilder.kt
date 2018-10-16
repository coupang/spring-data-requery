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

package org.springframework.data.requery.kotlin.repository.query

import io.requery.query.Condition
import io.requery.query.NamedExpression
import io.requery.query.Result
import io.requery.query.element.LogicalOperator
import io.requery.query.element.QueryElement
import mu.KLogging
import mu.KotlinLogging
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING
import org.springframework.data.domain.ExampleMatcher.StringMatcher.DEFAULT
import org.springframework.data.domain.ExampleMatcher.StringMatcher.ENDING
import org.springframework.data.domain.ExampleMatcher.StringMatcher.EXACT
import org.springframework.data.domain.ExampleMatcher.StringMatcher.STARTING
import org.springframework.data.requery.kotlin.extractGetterSetter
import org.springframework.data.requery.kotlin.findEntityMethods
import org.springframework.data.requery.kotlin.foldConditions
import org.springframework.data.requery.kotlin.isAssociatedAnnotatedElement
import org.springframework.data.requery.kotlin.isEmbeddedAnnoatedElement
import org.springframework.data.requery.kotlin.isKeyAnnoatedElement
import org.springframework.data.requery.kotlin.isTransientAnnotatedElement
import org.springframework.data.requery.kotlin.namedExpressionOf
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.data.support.ExampleMatcherAccessor
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
import org.springframework.util.LinkedMultiValueMap
import java.lang.reflect.Field

private val log = KotlinLogging.logger { }

/**
 * Query by {@link org.springframework.data.domain.Example} 을 수행하기 위해,
 * Example 을 이용하여 [io.requery.query.WhereAndOr] 를 빌드하도록 합니다.
 *
 * @author debop
 */
@Suppress("UNCHECKED_CAST")
fun <E : Any> QueryElement<out Any>.applyExample(example: Example<E>): QueryElement<out Any> {

    log.debug { "Apply example to query element. exampe=$example" }

    val matcher = example.matcher
    val conditions = QueryByExampleBuilder.buildConditions(example, ExampleMatcherAccessor(matcher))

    val condition = when {
        matcher.isAllMatching -> conditions.foldConditions(LogicalOperator.AND)
        matcher.isAnyMatching -> conditions.foldConditions(LogicalOperator.OR)
        else -> null
    }

    return condition?.let {
        this.where(it).unwrap() // as QueryElement<out Result<E>>
    } ?: this
}

object QueryByExampleBuilder : KLogging() {

    val entityFields = LinkedMultiValueMap<Class<*>, Field>()

    // TODO : rename to applyExample
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> build(base: QueryElement<out Result<E>>, example: Example<E>): QueryElement<out Result<E>> {

        val matcher = example.matcher
        val conditions = buildConditions(example, ExampleMatcherAccessor(matcher))

        val condition = when {
            matcher.isAllMatching -> conditions.foldConditions(LogicalOperator.AND)
            matcher.isAnyMatching -> conditions.foldConditions(LogicalOperator.OR)
            else -> conditions.foldConditions(LogicalOperator.AND)
        }

        return condition?.let {
            base.where(condition).unwrap() as QueryElement<out Result<E>>
        } ?: base
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Any> buildConditions(example: Example<E>,
                                  accessor: ExampleMatcherAccessor): List<Condition<E, *>> {

        log.debug { "Build conditions... example=$example" }

        val conditions = arrayListOf<Condition<E, *>>()

        val beanWrapper = DirectFieldAccessFallbackBeanWrapper(example.probe as Any)

        val methods = example.probeType.findEntityMethods()

        log.trace { "entity methods size=${methods.size}" }

        methods
            .asSequence()
            .filterNot {
                // Query By Example 에서 지원하지 못하는 Field 들은 제외합니다.
                // NOTE: Kotlin interface entity 는 get annotation을 사용하므로 method에 대해서도 처리해야 한다.
                it.isKeyAnnoatedElement() ||
                it.isAssociatedAnnotatedElement() ||
                it.isEmbeddedAnnoatedElement() ||
                it.isTransientAnnotatedElement()
            }
            .filterNot { accessor.isIgnoredPath(it.extractGetterSetter()) }
            .toList()
            .forEach {
                val fieldName = it.extractGetterSetter()
                val fieldType = it.returnType as Class<Any>
                val fieldValue = beanWrapper.getPropertyValue(fieldName)

                log.trace { "Get condition from Example. method name=$fieldName, value=$fieldValue, type=$fieldType" }

                val expr: NamedExpression<Any> = namedExpressionOf(fieldName, fieldType)

                when {
                    fieldValue == null ->
                        if(accessor.nullHandler == ExampleMatcher.NullHandler.INCLUDE) {
                            conditions.add(expr.isNull as Condition<E, *>)
                        }

                    fieldType == String::class.java ->
                        conditions.add(buildStringCondition(accessor,
                                                            expr as NamedExpression<String>,
                                                            fieldName,
                                                            fieldValue as String))

                    else -> {

                        conditions.add(expr.eq(fieldValue) as Condition<E, *>)
                    }
                }
            }

        log.debug { "Build up conditions. conditions size=${conditions.size}" }
        return conditions
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    fun <E : Any> buildStringCondition(accessor: ExampleMatcherAccessor,
                                       expression: NamedExpression<String>,
                                       fieldName: String,
                                       fieldValue: String): Condition<E, *> {

        val ignoreCase = accessor.isIgnoreCaseForPath(fieldName)
        log.trace { "Matching with ignoreCase? $ignoreCase" }

        val matcher = accessor.getStringMatcherForPath(fieldName)
        val fieldExpr = when {
            ignoreCase -> expression.function("Lower")
            else -> expression
        }

        return when(matcher) {
            DEFAULT, EXACT ->
                when {
                    ignoreCase -> fieldExpr.eq(fieldValue.toLowerCase())
                    else -> fieldExpr.eq(fieldValue)
                }
            CONTAINING ->
                when {
                    ignoreCase -> fieldExpr.like("%${fieldValue.toLowerCase()}%")
                    else -> fieldExpr.like("%$fieldValue%")
                }
            STARTING ->
                when {
                    ignoreCase -> fieldExpr.like(fieldValue.toLowerCase() + "%")
                    else -> fieldExpr.like("$fieldValue%")
                }
            ENDING ->
                when {
                    ignoreCase -> fieldExpr.like("%" + fieldValue.toLowerCase())
                    else -> fieldExpr.like("%$fieldValue")
                }

            else ->
                throw IllegalArgumentException("Unsupported STringMatcher $matcher")
        } as Condition<E, *>

    }
}

