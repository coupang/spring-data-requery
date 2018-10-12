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

package org.springframework.data.requery.kotlin.converters

import io.requery.Converter

/**
 * [IntArray] 를 String 으로 저장하는 [Converter] 입니다.
 *
 * @author debop
 * @since 18. 7. 2
 */
class IntArrayToStringConverter : Converter<IntArray, String> {

    override fun getPersistedType(): Class<String> = String::class.java

    override fun getMappedType(): Class<IntArray> = IntArray::class.java

    override fun getPersistedSize(): Int? = null


    override fun convertToMapped(type: Class<out IntArray>?, value: String?): IntArray? {
        return value?.let {
            it.split(",").map { it.toInt() }.toIntArray()
        }
    }

    override fun convertToPersisted(value: IntArray?): String? {
        return value?.let {
            it.joinToString(",") { it.toString() }
        }
    }
}