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

package org.springframework.data.requery.domain.superclass;

import org.junit.Test;
import org.springframework.data.requery.domain.AbstractDomainTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Diego on 2018. 6. 13..
 */
public class InheritanceTest extends AbstractDomainTest {

    @Test
    public void create_derived_class() {

        RelatedEntity related = new RelatedEntity();
        related.setId(1L);
        requeryOperations.insert(related);

        DerivedAEntity derivedA = new DerivedAEntity();
        derivedA.setId(2L);
        derivedA.getRelated().add(related);

        requeryOperations.insert(derivedA);

        DerivedBEntity derivedB = new DerivedBEntity();
        derivedB.setId(3L);
        derivedB.getRelated().add(related);

        requeryOperations.insert(derivedB);

        DerivedAEntity loadedA = requeryOperations.findById(DerivedAEntity.class, 2L);
        assertThat(loadedA.getId()).isEqualTo(derivedA.getId());
        assertThat(loadedA.getRelated().iterator().next()).isEqualTo(related);

        DerivedBEntity loadedB = requeryOperations.findById(DerivedBEntity.class, 3L);
        assertThat(loadedB.getId()).isEqualTo(derivedB.getId());
        assertThat(loadedB.getRelated().iterator().next()).isEqualTo(related);
    }
}
