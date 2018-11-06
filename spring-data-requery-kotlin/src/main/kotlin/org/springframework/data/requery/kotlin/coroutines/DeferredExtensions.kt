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

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import java.time.Duration
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

/**
 * Requery 사용 시 사용하는 기본 [CoroutineDispatcher]입니다.
 * Transaction이 thread bound 이므로 [Dispatchers.Unconfined] 를 사용합니다.
 */
internal val defaultCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default

suspend fun <T> CompletionStage<T>.asDeferred(): Deferred<T> {
    return coroutineScope {
        async {
            this@asDeferred.toCompletableFuture().join()
        }
    }
}


suspend fun <T> CompletionStage<T>.toDeferred(): Deferred<T> {
    return coroutineScope {
        async {
            this@toDeferred.toCompletableFuture().join()
        }
    }
}

suspend fun <T> Iterable<Deferred<T>>.flatten(): Deferred<List<T>> {
    return coroutineScope {
        async {
            this@flatten.map { runBlocking { it.await() } }
        }
    }
}

suspend fun <T> Stream<Deferred<T>>.flatten(): Deferred<Stream<T>> {
    return coroutineScope {
        async {
            this@flatten.map { runBlocking { it.await() } }
        }
    }
}

suspend fun <T : Any> Deferred<T>.await(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T? =
    withTimeout(unit.toMillis(time)) { await() }

suspend fun <T : Any> Deferred<T>.await(duration: Duration): T? =
    withTimeout(duration.toMillis()) { await() }

suspend fun <T : Any> Deferred<T>.awaitOrNull(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T? =
    try {
        withTimeout(unit.toMillis(time)) { await() }
    } catch(e: Exception) {
        null
    }

suspend fun <T : Any> Deferred<T>.awaitOrNull(duration: Duration): T? =
    try {
        withTimeout(duration.toMillis()) { await() }
    } catch(e: Exception) {
        null
    }

suspend fun <T : Any> Deferred<T>.awaitOrDefault(defaultValue: T?, time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T? =
    try {
        withTimeout(unit.toMillis(time)) { await() }
    } catch(e: CancellationException) {
        defaultValue
    }

suspend fun <T : Any> Deferred<T>.awaitOrDefault(defaultValue: T?, duration: Duration): T? =
    try {
        withTimeout(duration.toMillis()) { await() }
    } catch(e: CancellationException) {
        defaultValue
    }