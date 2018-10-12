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

package org.springframework.data.requery.kotlin.coroutines

import io.requery.TransactionIsolation
import io.requery.sql.KotlinEntityDataStore
import mu.KotlinLogging

/**
 * [KotlinCoroutineRequeryOperations] 의 구현체 입니다.
 *
 * @author debop
 * @since 18. 6. 2
 */
class KotlinCoroutineRequeryTemplate(override val dataStore: KotlinEntityDataStore<Any>) : KotlinCoroutineRequeryOperations {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    override suspend fun <T : Any> withTransaction(isolation: TransactionIsolation?, block: KotlinCoroutineRequeryOperations.() -> T): T {
        return isolation?.let {
            dataStore.withTransaction(isolation) {
                block.invoke(this@KotlinCoroutineRequeryTemplate)
            }
        } ?: dataStore.withTransaction { block.invoke(this@KotlinCoroutineRequeryTemplate) }

    }
}