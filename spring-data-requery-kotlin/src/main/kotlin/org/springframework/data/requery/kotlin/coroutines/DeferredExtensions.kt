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

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionStage
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.coroutines.experimental.CoroutineContext

fun <T : Any?> T.toDeferred(context: CoroutineContext = Dispatchers.Default): Deferred<T> {
    return GlobalScope.async(context) { this@toDeferred }
}

fun <T> Future<T>.asDeferred(context: CoroutineContext = Dispatchers.Default): Deferred<T> {
    return GlobalScope.async(context) { this@asDeferred.get() }
}

fun <T> CompletionStage<T>.toDeferred(context: CoroutineContext = Dispatchers.Default): Deferred<T> {
    return GlobalScope.async(context) { this@toDeferred.toCompletableFuture().join() }
}

fun <T> CompletionStage<T>.asDeferred(context: CoroutineContext = Dispatchers.Default): Deferred<T> {
    return GlobalScope.async(context) { this@asDeferred.toCompletableFuture().join() }
}

suspend fun <T> Iterable<Deferred<T>>.flatten(context: CoroutineContext = Dispatchers.Default): Deferred<List<T>> {
    return this@flatten.map { it.await() }.toDeferred(context)
}

fun <T> Stream<Deferred<T>>.flatten(context: CoroutineContext = Dispatchers.Default): Deferred<Stream<T>> {
    return GlobalScope.async(context) {
        this@flatten.map {
            it.getCompleted()
        }
    }
}

suspend fun <T : Any?> Deferred<T>.await(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T =
    withTimeout(unit.toMillis(time)) { await() }

@Suppress("UNCHECKED_CAST")
suspend fun <T : Any?> Deferred<T>.awaitSilence(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T =
    try {
        withTimeout(unit.toMillis(time)) { await() }
    } catch(e: Exception) {
        null as T
    }

suspend fun <T : Any?> Deferred<T>.awaitOrDefault(defaultValue: T, time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): T =
    try {
        withTimeout(unit.toMillis(time)) { await() }
    } catch(e: CancellationException) {
        defaultValue
    }