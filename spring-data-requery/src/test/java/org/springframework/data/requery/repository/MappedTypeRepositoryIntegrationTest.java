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

package org.springframework.data.requery.repository;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.requery.domain.sample.ConcreteType1;
import org.springframework.data.requery.domain.sample.ConcreteType2;
import org.springframework.data.requery.repository.sample.ConcreteRepository1;
import org.springframework.data.requery.repository.sample.ConcreteRepository2;
import org.springframework.data.requery.repository.sample.SampleConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MappedTypeRepositoryIntegrationTest
 *
 * @author debop
 * @since 18. 6. 28
 */
@Transactional
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SampleConfig.class })
public class MappedTypeRepositoryIntegrationTest {

    @Autowired ConcreteRepository1 concreteRepository1;
    @Autowired ConcreteRepository2 concreteRepository2;

    @Ignore("Raw Query구문에 Entity의 Table명을 추론하는 기능은 지원하지 않습니다.")
    @Test
    public void supportForExpressionBasedQueryMethods() {

        concreteRepository1.save(new ConcreteType1("foo"));
        concreteRepository2.save(new ConcreteType2("foo"));

        List<ConcreteType1> concretes1 = concreteRepository1.findAllByAttribute1("foo");
        List<ConcreteType2> concretes2 = concreteRepository2.findAllByAttribute1("foo");

        assertThat(concretes1).hasSize(1);
        assertThat(concretes2).hasSize(1);
    }

    @Ignore("Raw Query구문에 Entity의 Table명을 추론하는 기능은 지원하지 않습니다.")
    @Test
    public void supportForPaginationCustomQueryMethodsWithEntityExpression() {

        concreteRepository1.save(new ConcreteType1("foo"));
        concreteRepository2.save(new ConcreteType2("foo"));

        Page<ConcreteType2> page = concreteRepository2
            .findByAttribute1Custom("foo",
                                    PageRequest.of(0, 10, Sort.Direction.DESC, "attribute1"));

        assertThat(page.getNumberOfElements()).isEqualTo(1);
    }
}
