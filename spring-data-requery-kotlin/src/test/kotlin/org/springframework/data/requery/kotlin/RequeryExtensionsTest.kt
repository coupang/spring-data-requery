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

package org.springframework.data.requery.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.sample.UserEntity

/**
 * org.springframework.data.requery.RequeryExtensionsTest
 *
 * @author debop
 */
class RequeryExtensionsTest : AbstractDomainTest() {

    @Test
    fun `retrieve Entity Context`() {
        val entityContext = kotlinDataStore.getEntityContext()
        assertThat(entityContext).isNotNull

        assertThat(operations.entityContext).isEqualTo(entityContext)
    }

    @Test
    fun `retrieve EntityModel`() {
        val entityModel = kotlinDataStore.getEntityModel()
        assertThat(entityModel).isNotNull
        assertThat(entityModel.name).isEqualTo("default")

        assertThat(operations.entityModel).isEqualTo(entityModel)
    }

    @Test
    fun `retrieve entity types from EntityDataStore`() {
        val types = kotlinDataStore.getEntityTypes()
        assertThat(types).isNotEmpty.contains(UserEntity.`$TYPE`)
    }

    @Test
    fun `retrieve entity classes from EntityDataStore`() {
        val classes = kotlinDataStore.getEntityClasses()
        assertThat(classes).isNotEmpty.contains(UserEntity::class.java)
    }

    @Test
    fun `retrieve type from EntityDataStore`() {
        assertThat(kotlinDataStore.getType(UserEntity::class.java)).isEqualTo(UserEntity.`$TYPE`)
        assertThat(kotlinDataStore.getType(UserEntity::class)).isEqualTo(UserEntity.`$TYPE`)
    }

    @Test
    fun `get Identifier attribute from entity`() {

        val keyAttrs = kotlinDataStore.getKeyAttributes(UserEntity::class.java)
        assertThat(keyAttrs).hasSize(1)
        assertThat(keyAttrs.first().classType).isEqualTo(Integer::class.java)
    }

    @Test
    fun `get single id attribute from domain class`() {

        val keyAttr = kotlinDataStore.getSingleKeyAttribute(UserEntity::class.java)
        assertThat(keyAttr).isNotNull
        assertThat(keyAttr.classType).isEqualTo(Integer::class.java)
    }
}