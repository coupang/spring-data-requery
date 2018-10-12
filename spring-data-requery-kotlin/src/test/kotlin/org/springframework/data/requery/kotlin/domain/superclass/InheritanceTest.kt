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

package org.springframework.data.requery.kotlin.domain.superclass

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.superclass.model.DerivedAEntity
import org.springframework.data.requery.kotlin.domain.superclass.model.DerivedBEntity
import org.springframework.data.requery.kotlin.domain.superclass.model.RelatedEntity

/**
 * org.springframework.data.requery.kotlin.domain.superclass.InheritanceTest
 *
 * @author debop
 */
class InheritanceTest : AbstractDomainTest() {

    @Test
    fun `create derived class`() {

        val related = RelatedEntity()
        operations.insert(related)

        val derivedA = DerivedAEntity()
        derivedA.relateds.add(related)
        operations.insert(derivedA)

        val derivedB = DerivedBEntity().apply { flag = false }
        operations.upsert(derivedB)

        val loaded = operations.findById(DerivedAEntity::class, derivedA.id)
        assertThat(loaded).isNotNull
        assertThat(loaded?.id).isEqualTo(derivedA.id)
        assertThat(loaded?.relateds?.firstOrNull()?.id).isEqualTo(related.id)
    }
}
