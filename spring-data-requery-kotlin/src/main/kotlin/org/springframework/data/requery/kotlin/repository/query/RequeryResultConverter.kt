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

import io.requery.query.Tuple
import mu.KLogging

/**
 * Requery의 Query 결과 값을 원하는 수형으로 변환하는 기능을 제공합니다.
 */
object RequeryResultConverter : KLogging() {

    /**
     * Requery 퀄리 결과를 원하는 수형으로 변환합니다.
     * 결과가 Entity가 아니라 Tuple 인 경우에 count 나 aggregate 를 표현할 수 있으므로 그 값을 추출흡니다.
     *
     * @param result query result
     * @param defaultValue default value if convert is failed
     */
    fun convert(result: Any?, defaultValue: Any? = null): Any? {

        logger.trace { "Convert result. result=$result, defaultValue=$defaultValue" }

        return try {
            when(result) {
                is Tuple -> when {
                    result.count() == 1 -> {
                        val column = result.get<Any>(0)
                        (column as? Number)?.toInt() ?: column.toString()
                    }
                    else -> result
                }
                else -> result
            }
        } catch(ignored: Exception) {
            logger.warn(ignored) { "Fail to convert result[$result], return [$defaultValue]" }
            defaultValue
        }
    }

    /**
     * Requery 퀄리 결과를 원하는 수형으로 변환합니다.
     * 결과가 Entity가 아니라 Tuple 인 경우에 count 나 aggregate 를 표현할 수 있으므로 그 값을 추출흡니다.
     *
     * @param result query result
     * @param defaultValue default value if convert is failed
     */
    inline fun <reified T : Any> asValue(result: Any?, defaultValue: T? = null): T? {
        logger.trace { "Convert result. result=$result, defaultValue=$defaultValue" }

        return try {
            when(result) {
                is Tuple -> when {
                    result.count() == 1 -> result.get<T>(0)
                    else -> result as? T
                }
                else -> result as? T
            }
        } catch(ignored: Exception) {
            logger.warn(ignored) { "Fail to convert result[$result], return [$defaultValue]" }
            defaultValue
        }
    }
}