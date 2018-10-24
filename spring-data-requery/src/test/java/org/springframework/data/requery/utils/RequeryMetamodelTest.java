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

package org.springframework.data.requery.utils;

import org.junit.Test;
import org.springframework.data.requery.domain.Models;
import org.springframework.data.requery.domain.sample.AbstractUser;
import org.springframework.data.requery.domain.sample.Item;
import org.springframework.data.requery.domain.sample.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequeryMetamodelTest
 *
 * @author debop (Sunghyouk Bae)
 * @since 18. 10. 23
 */
public class RequeryMetamodelTest {

    private RequeryMetamodel metamodel = new RequeryMetamodel(Models.DEFAULT);

    @Test
    public void createRequeryMetamodel() {
        assertThat(metamodel).isNotNull();
    }

    @Test
    public void requery_entity_is_managed() {
        assertThat(metamodel.isRequeryManaged(User.class)).isTrue();
    }

    @Test
    public void abstract_entity_is_not_managed() {
        // Abstract class는 Managed type이 아니다.
        assertThat(metamodel.isRequeryManaged(AbstractUser.class)).isFalse();
    }

    @Test
    public void no_requery_entity_is_not_managed() {
        assertThat(metamodel.isRequeryManaged(RequeryMetamodel.class)).isFalse();
    }

    @Test
    public void entity_has_single_id_attribute() {
        assertThat(metamodel.isSingleIdAttribute(User.class, "id", Integer.class)).isTrue();
    }

    @Test
    public void entity_has_multiple_id_attribute() {
        assertThat(metamodel.isSingleIdAttribute(Item.class, "id", Integer.class)).isFalse();
    }
}
