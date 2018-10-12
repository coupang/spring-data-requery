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

import io.requery.query.Result
import io.requery.query.ResultDelegate
import io.requery.query.element.QueryElement
import io.requery.query.element.QueryWrapper
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

/**
 * Kotlin Coroutines를 이용한 비동기 결과를 표현합니다.
 *
 * @author debop
 * @since 18. 5. 16
 */
class DeferredResult<E>(delegate: Result<E>,
                        val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default)
    : ResultDelegate<E>(delegate), QueryWrapper<E> {

    inline fun <V> toDefered(crossinline block: DeferredResult<E>.() -> V): Deferred<V> {
        return GlobalScope.async(coroutineDispatcher) { block() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun unwrapQuery(): QueryElement<E> {
        return (delegate as QueryWrapper<E>).unwrapQuery()
    }
}