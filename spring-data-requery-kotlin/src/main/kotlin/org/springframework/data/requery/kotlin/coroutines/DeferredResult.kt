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
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.coroutineScope

/**
 * Kotlin Coroutines를 이용한 비동기 방식의 작업의 [Result]를 표현합니다.
 *
 * @author debop
 * @since 18. 5. 16
 */
class DeferredResult<E>(delegate: Result<E>)
    : ResultDelegate<E>(delegate), QueryWrapper<E> {

    /**
     * 비동기 ResultSet 의 결과를 기다렸다가 원하는 형태로 변환하는 `block` 을 실행합니다.
     *
     * <code>
     * runBlocking {
     *     val users = coroutineEntityStore.select(User::class).get().await { toList() }
     * }
     * </code>
     * @param V
     * @param block
     */
    suspend inline fun <reified V : Any> await(crossinline block: suspend DeferredResult<E>.() -> V): V {
        return toDeferred { block() }.await()
    }

    suspend inline fun <reified V : Any> toDeferred(crossinline block: suspend (DeferredResult<E>) -> V): Deferred<V> {
        return coroutineScope {
            async {
                block(this@DeferredResult)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun unwrapQuery(): QueryElement<E> {
        return (delegate as QueryWrapper<E>).unwrapQuery()
    }
}