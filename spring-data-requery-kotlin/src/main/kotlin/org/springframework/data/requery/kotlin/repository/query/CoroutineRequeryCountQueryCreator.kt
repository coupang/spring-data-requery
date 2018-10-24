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
import io.requery.query.function.Count
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.ReturnedType
import org.springframework.data.repository.query.parser.PartTree
import org.springframework.data.requery.kotlin.Requery
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.unwrap

/**
 * Coroutine을 사용하여 Count Query를 생성합니다.
 *
 * @see CoroutineRequeryQueryCreator
 *
 * @author debop
 * @since 18. 10. 16
 */
class CoroutineRequeryCountQueryCreator(operations: CoroutineRequeryOperations,
                                        provider: ParameterMetadataProvider,
                                        returnedType: ReturnedType,
                                        tree: PartTree)
    : CoroutineRequeryQueryCreator(operations, provider, returnedType, tree) {

    override suspend fun createQueryElement(type: ReturnedType): Requery {
        return operations.select(Count.count(type.domainType)).unwrap()
    }

    override suspend fun complete(criteria: LogicalCondition<out Any, *>?,
                                  sort: Sort,
                                  base: Requery): Requery {
        var countQuery = operations.select(Count.count(domainKlass.java)).unwrap()
        if(criteria != null) {
            countQuery = countQuery.where(criteria).unwrap()
        }
        return countQuery
    }
}
