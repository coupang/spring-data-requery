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

package org.springframework.data.requery.domain.blob;

import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * org.springframework.data.requery.domain.blob.BinaryDataTest
 *
 * @author debop
 * @since 18. 6. 4
 */
public class BinaryDataTest extends AbstractDomainTest {

    @Test
    public void saveBlobData() {
        byte[] bytes = new byte[8192];
        rnd.nextBytes(bytes);

        BinaryData binData = new BinaryData();
        binData.setName("binary data");
        binData.setPicture(bytes);

        requeryTemplate.insert(binData);
        assertThat(binData).isNotNull();

        BinaryData loaded = requeryTemplate.findById(BinaryData.class, binData.id);
        assertThat(loaded).isNotNull().isEqualTo(binData);
        assertThat(loaded.getPicture()).isNotNull().isEqualTo(binData.getPicture());

        rnd.nextBytes(bytes);
        loaded.setPicture(bytes);
        requeryTemplate.upsert(loaded);
    }
}
