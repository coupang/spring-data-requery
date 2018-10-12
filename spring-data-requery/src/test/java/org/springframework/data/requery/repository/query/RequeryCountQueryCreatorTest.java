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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.sample.Role;
import org.springframework.data.requery.domain.sample.User;
import org.springframework.data.requery.repository.config.InfrastructureConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * org.springframework.data.requery.repository.query.RequeryCountQueryCreatorTest
 *
 * @author debop
 * @since 18. 6. 26
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { InfrastructureConfig.class })
public class RequeryCountQueryCreatorTest {

    @Autowired RequeryOperations operations;

    @Test
    public void distinctFlagOnCountQueryIssuesCountDistinct() throws Exception {
        //
    }

    interface SomeRepository extends Repository<User, Integer> {
        void findDistinctByRolesIn(List<Role> roles);
    }
}
