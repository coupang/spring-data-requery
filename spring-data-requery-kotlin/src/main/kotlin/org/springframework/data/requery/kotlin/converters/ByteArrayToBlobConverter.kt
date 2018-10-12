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
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob

/**
 * [ByteArray]를 [Blob]로 저장하도록 하는 Converter
 *
 * @author debop
 * @since 18. 7. 2
 */
class ByteArrayToBlobConverter : Converter<ByteArray, Blob> {

    override fun getPersistedType(): Class<Blob> = Blob::class.java

    override fun getMappedType(): Class<ByteArray> = ByteArray::class.java

    override fun getPersistedSize(): Int? = null

    override fun convertToMapped(type: Class<out ByteArray>?, value: Blob?): ByteArray? {
        return value?.binaryStream?.readBytes()
    }

    override fun convertToPersisted(value: ByteArray?): Blob? {
        return value?.let { SerialBlob(it) }
    }

}