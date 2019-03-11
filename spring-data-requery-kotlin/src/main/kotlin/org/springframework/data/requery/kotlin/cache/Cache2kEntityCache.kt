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

package org.springframework.data.requery.kotlin.cache

import io.requery.EntityCache
import io.requery.sql.EntityDataStore
import mu.KLogging
import org.cache2k.Cache
import org.cache2k.Cache2kBuilder
import org.cache2k.configuration.Cache2kConfiguration
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache2k를 이용하여 Requery entity를 cache 합니다.
 * Requery Entity는 분산환경에서의 Cache는 사용할 수 없습니다 (DTO로 만든 후 캐시해야 합니다)
 *
 * @author debop
 * @since 18. 7. 2
 */
class Cache2kEntityCache @JvmOverloads constructor(
    val configuration: Cache2kConfiguration<Any, Any?> = DEFAULT_CONFIGURATION) : EntityCache {

    companion object : KLogging() {

        private val DEFAULT_CONFIGURATION: Cache2kConfiguration<Any, Any?> =
            Cache2kConfiguration.of(Any::class.java, Any::class.java)
                .apply {
                    entryCapacity = 20000L
                    isEternal = true
                    isKeepDataAfterExpired = false
                    isBoostConcurrency = true
                    retryInterval = 10
                    maxRetryInterval = 1000L
                }

        private val cacheManager: MutableMap<Class<*>, Cache<Any, Any?>> = ConcurrentHashMap()
        private val syncObj: Any = Any()
    }

    var dataStore: EntityDataStore<Any>? = null

    val Class<*>.cache: Cache<Any, Any?>
        get() {
            return cacheManager.computeIfAbsent(this) { clazz ->
                synchronized(syncObj) {
                    logger.debug { "Create Cache2k cache for type [${this.name}]" }

                    Cache2kBuilder.of(configuration).name(clazz.name).run {
                        if(dataStore != null) {
                            loader { dataStore!!.findByKey(clazz, it) }
                        }
                        build()
                    }
                }
            }
        }

    override fun contains(type: Class<*>?, key: Any?): Boolean {
        return type?.cache?.containsKey(key) ?: false
    }

    override fun invalidate(type: Class<*>?) {
        type?.cache?.removeAll()
    }

    override fun invalidate(type: Class<*>?, key: Any?) {
        key?.let { type?.cache?.remove(key) }
    }

    override fun clear() {
        synchronized(syncObj) {
            cacheManager.forEach { _, cache ->
                cache.clearAndClose()
            }
            cacheManager.clear()
        }
    }

    override fun <T : Any> put(type: Class<T>?, key: Any?, value: T?) {
        value?.let {
            key?.let { type?.cache?.put(key, value) }
        } ?: invalidate(type, key)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(type: Class<T>?, key: Any?): T? {
        return key?.let {
            type?.cast(type.cache.get(key))
        }
    }

}