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


object RequeryResultConverter : KLogging() {

    fun convert(result: Any?, defaultValue: Any? = null): Any? {

        logger.trace { "Convert result. result=$result, defaultValue=$defaultValue" }

        return try {
            if(result is Tuple) {
                if(result.count() == 1) {
                    val column = result.get<Any>(0)
                    (column as? Number)?.toInt() ?: column.toString()
                } else {
                    result
                }
            } else result
        } catch(ignored: Exception) {
            logger.warn(ignored) { "Fail to convert result[$result], return [$defaultValue]" }
            defaultValue
        }
    }

    inline fun <reified T : Any> convertGeneric(result: Any?, defaultValue: T? = null): T? {
        logger.trace { "Convert result. result=$result, defaultValue=$defaultValue" }

        return try {
            if(result is Tuple) {
                if(result.count() == 1) {
                    // val column = result.get<T>(0)
                    // (column as? Number)?.toInt() ?: column.toString()
                    result.get<T>(0)
                } else {
                    result as? T
                }
            } else result as? T
        } catch(ignored: Exception) {
            logger.warn(ignored) { "Fail to convert result[$result], return [$defaultValue]" }
            defaultValue
        }
    }
}