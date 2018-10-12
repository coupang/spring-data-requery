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

package org.springframework.data.requery.repository.query;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.data.requery.domain.basic.AbstractBasicUser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * org.springframework.data.requery.repository.query.DefaultRequeryEntityMetadataTest
 *
 * @author debop
 * @since 18. 6. 8
 */
public class DefaultRequeryEntityMetadataTest {

    @Test
    public void rejects_null_domainType() {
        Assertions.assertThatThrownBy(() -> {

            new DefaultRequeryEntityMetadata(null);

        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void returnsConfiguredType() {
        DefaultRequeryEntityMetadata<Foo> metadata = new DefaultRequeryEntityMetadata<>(Foo.class);
        assertThat(metadata.getJavaType()).isEqualTo(Foo.class);
    }

    @Test
    public void returnSimpleClassNameAsEntityNameByDefault() {
        DefaultRequeryEntityMetadata<Foo> metadata = new DefaultRequeryEntityMetadata<>(Foo.class);
        assertThat(metadata.getEntityName()).isEqualTo(Foo.class.getSimpleName());
    }

    @Test
    public void returnCustomizedEntityNameIfConfigured() {
        DefaultRequeryEntityMetadata<AbstractBasicUser> metadata = new DefaultRequeryEntityMetadata<>(AbstractBasicUser.class);
        assertThat(metadata.getEntityName()).isEqualTo("BasicUser");
    }


    static class Foo {}

}
