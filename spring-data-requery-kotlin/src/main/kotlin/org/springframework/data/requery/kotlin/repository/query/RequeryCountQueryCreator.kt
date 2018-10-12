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

import io.requery.query.LogicalCondition
import io.requery.query.element.QueryElement
import io.requery.query.function.Count
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.repository.query.parser.PartTree
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.unwrap

/**
 * org.springframework.data.requery.kotlin.repository.query.RequeryCountQueryCreator
 *
 * @author debop
 */
class RequeryCountQueryCreator(operations: RequeryOperations,
                               provider: ParameterMetadataProvider,
                               returnedType: ReturnedType,
                               tree: PartTree) : RequeryQueryCreator(operations, provider, returnedType, tree) {

    override fun createQueryElement(type: ReturnedType): QueryElement<out Any> {
        return operations
            .select(Count.count(type.domainType))
            .unwrap()
    }

    override fun complete(criteria: LogicalCondition<out Any, *>?,
                          sort: Sort,
                          base: QueryElement<out Any>): QueryElement<out Any> {
        return when(criteria) {
            null -> operations.select(Count.count(domainClass)).unwrap()
            else -> operations.select(Count.count(domainClass)).where(criteria).unwrap()
        }
    }
}