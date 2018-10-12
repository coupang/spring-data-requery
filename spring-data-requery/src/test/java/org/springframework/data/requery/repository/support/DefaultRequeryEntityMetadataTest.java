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

package org.springframework.data.requery.repository.support;

import org.junit.Test;
import org.springframework.data.requery.domain.basic.AbstractBasicGroup;
import org.springframework.data.requery.domain.basic.AbstractBasicUser;
import org.springframework.data.requery.domain.model.AbstractGroup;
import org.springframework.data.requery.repository.query.DefaultRequeryEntityMetadata;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultRequeryEntityMetadataTest {

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullDomainType() {
        new DefaultRequeryEntityMetadata(null);
    }

    @Test
    public void returnsConfiguredType() {
        DefaultRequeryEntityMetadata<AbstractGroup> metadata = new DefaultRequeryEntityMetadata<>(AbstractGroup.class);
        assertThat(metadata.getJavaType()).isEqualTo(AbstractGroup.class);
    }

    @Test
    public void returnsSimpleClassNameAsEntityNameByDefault() {
        DefaultRequeryEntityMetadata<AbstractBasicGroup> metadata = new DefaultRequeryEntityMetadata<>(AbstractBasicGroup.class);
        assertThat(metadata.getEntityName()).isEqualTo(AbstractBasicGroup.class.getSimpleName());
    }

    @Test
    public void returnsCustomizedEntityNameIfConfigured() {
        DefaultRequeryEntityMetadata<AbstractBasicUser> metadata = new DefaultRequeryEntityMetadata<>(AbstractBasicUser.class);
        assertThat(metadata.getEntityName()).isEqualTo("BasicUser");
        assertThat(metadata.getModelName()).isEqualTo("default");
    }
}
