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

import io.requery.query.Scalar
import io.requery.query.ScalarDelegate
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.runBlocking

/**
 * Kotlin Coroutines 을 이용하여 Scala 수형의 질의 반환 값을 받을 때 사용하는 클래스입니다.
 *
 * @author debop
 * @since 18. 5. 16
 */
class DeferredScalar<E>(delegate: Scalar<E>) : ScalarDelegate<E>(delegate) {

    /**
     * Scalar 수형의 결과 값을 받을 때 까지 기다립니다.
     *
     * <code>
     *     runBlocking {
     *          val deleteCount:Int = coroutineEntityStore.delete(user).await()
     *     }
     * </code>
     */
    suspend fun await(): E = toDeferred().await()

    override fun value(): E = runBlocking { await() }

    suspend fun toDeferred(): Deferred<E> {
        return coroutineScope {
            async {
                call()
            }
        }
    }
}