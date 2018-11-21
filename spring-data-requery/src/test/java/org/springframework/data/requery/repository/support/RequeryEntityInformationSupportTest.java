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

import io.requery.meta.Attribute;
import io.requery.meta.EntityModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.basic.AbstractBasicUser;

import javax.annotation.Nullable;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequeryEntityInformationSupportTest
 *
 * @author debop
 * @since 18. 6. 12
 */
@RunWith(MockitoJUnitRunner.class)
public class RequeryEntityInformationSupportTest {

    @Mock RequeryOperations operations;
    @Mock EntityModel entityModel;

    @Test
    public void usesSimpleClassNameIfNoEntityNameGiven() throws Exception {

        RequeryEntityInformation<User, Long> information = new DummyRequeryEntityInformation<>(User.class);
        assertThat(information.getEntityName()).isEqualTo("User");
        assertThat(information.getModelName()).isNullOrEmpty();

        RequeryEntityInformation<AbstractBasicUser, Long> second = new DummyRequeryEntityInformation<>(AbstractBasicUser.class);
        assertThat(second.getEntityName()).isEqualTo("BasicUser");
        assertThat(second.getModelName()).isEqualTo("default");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsClassNotBeingFoundInMetamodel() {

        Mockito.when(operations.getEntityModel()).thenReturn(entityModel);
        RequeryEntityInformationSupport.getEntityInformation(User.class, operations);
    }

    static class User {

    }

    static class DummyRequeryEntityInformation<T, ID> extends RequeryEntityInformationSupport<T, ID> {

        public DummyRequeryEntityInformation(Class<T> domainClass) {
            super(domainClass);
        }

        @Override
        @Nullable
        public Attribute<? super T, ?> getIdAttribute() {
            return null;
        }

        @Override
        public boolean hasCompositeId() {
            return false;
        }

        @Override
        public Iterable<String> getIdAttributeNames() {
            return Collections.emptySet();
        }

        @Override
        @Nullable
        public Object getCompositeIdAttributeValue(Object id, String idAttribute) {
            return null;
        }

        @Override
        public ID getId(T entity) {
            return null;
        }

        @Override
        public Class<ID> getIdType() {
            return null;
        }
    }
}
