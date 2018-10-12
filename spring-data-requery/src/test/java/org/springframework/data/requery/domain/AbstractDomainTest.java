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

package org.springframework.data.requery.domain;

import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * org.springframework.data.requery.domain.AbstractDomainTest
 *
 * @author debop
 * @since 18. 6. 4
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { RequeryTestConfiguration.class })
public abstract class AbstractDomainTest {

    protected static Random rnd = new Random(System.currentTimeMillis());

    @Autowired
    protected EntityDataStore<Object> dataStore;

    @Autowired
    protected RequeryOperations requeryTemplate;

    @Test
    public void context_loading() {
        assertThat(dataStore).isNotNull();
        assertThat(requeryTemplate).isNotNull();
    }
}
