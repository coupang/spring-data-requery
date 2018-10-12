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

package org.springframework.data.requery.domain.basic;

import io.requery.query.Result;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;
import org.springframework.data.requery.domain.RandomData;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicEntityTest extends AbstractDomainTest {

    @Before
    public void setup() {
        requeryTemplate.deleteAll(BasicLocation.class);
        requeryTemplate.deleteAll(BasicGroup.class);
        requeryTemplate.deleteAll(BasicUser.class);
    }


    @Test
    public void insert_user_without_association() throws Exception {

        BasicUser user = RandomData.randomUser();
        requeryTemplate.insert(user);

        BasicUser loaded = requeryTemplate.findById(BasicUser.class, user.id);

        assertThat(loaded.getLastModifiedDate()).isNull();
        loaded.name = "updated";
        requeryTemplate.update(loaded);

        assertThat(loaded.getLastModifiedDate()).isNotNull();
    }

    @Test
    public void select_with_limit() throws Exception {
        BasicUser user = RandomData.randomUser();
        requeryTemplate.insert(user);

        // NOTE: 특정 컬럼만 가지고 온 후, 다른 컬럼을 참조하면, Lazy loading을 수행해준다.
        Result<BasicUser> result = requeryTemplate
            .select(BasicUser.class, BasicUser.ID, BasicUser.NAME)
            .limit(10)
            .get();

        BasicUser first = result.first();
        assertThat(first.getId()).isEqualTo(user.getId());
        assertThat(first.getName()).isEqualTo(user.getName());

        // NOTE: Lazy loading을 수행합니다 !!!
        assertThat(first.getAge()).isEqualTo(user.getAge());
    }
}
