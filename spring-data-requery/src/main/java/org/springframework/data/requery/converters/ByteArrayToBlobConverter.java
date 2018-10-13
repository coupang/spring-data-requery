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

package org.springframework.data.requery.converters;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StreamUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * {@link byte[]} 를 {@link Blob}로 저장하도록 하는 Converter
 *
 * @author debop
 */
@Slf4j
public class ByteArrayToBlobConverter implements io.requery.Converter<byte[], Blob> {

    @Override
    public Class<byte[]> getMappedType() {
        return byte[].class;
    }

    @Override
    public Class<Blob> getPersistedType() {
        return Blob.class;
    }

    @Nullable
    @Override
    public Integer getPersistedSize() {
        return null;
    }

    @Nullable
    @Override
    public Blob convertToPersisted(@Nullable byte[] value) {
        try {
            return (value != null) ? new SerialBlob(value) : null;
        } catch (SQLException e) {
            log.error("Fail to convert to Blob.", e);
            return null;
        }
    }

    @Nullable
    @Override
    public byte[] convertToMapped(Class<? extends byte[]> type, @Nullable Blob value) {
        try {
            return (value != null) ? StreamUtils.copyToByteArray(value.getBinaryStream()) : null;
        } catch (Exception e) {
            log.error("Fail to convert to byte[]", e);
            return null;
        }
    }
}
