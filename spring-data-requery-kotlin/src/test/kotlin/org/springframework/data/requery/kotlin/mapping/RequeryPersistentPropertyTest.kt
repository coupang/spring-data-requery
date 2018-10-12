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

package org.springframework.data.requery.kotlin.mapping

import com.nhaarman.mockito_kotlin.mock
import io.requery.Column
import io.requery.Embedded
import io.requery.Key
import io.requery.ManyToOne
import io.requery.OneToOne
import io.requery.Transient
import io.requery.meta.EntityModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.data.annotation.Version
import org.springframework.data.util.ClassTypeInformation

/**
 * RequeryPersistentPropertyTest
 *
 * @author debop
 */
@RunWith(MockitoJUnitRunner::class)
class RequeryPersistentPropertyTest {

    val entityModel = mock<EntityModel>()

    lateinit var context: RequeryMappingContext
    lateinit var entity: RequeryPersistentEntity<*>

    @Before
    fun setup() {
        context = RequeryMappingContext()
        entity = context.getRequiredPersistentEntity(Sample::class.java)
    }

    @Test
    fun `considers OneToOne mapped property association`() {
        val property = entity.getRequiredPersistentProperty("other")
        assertThat(property).isNotNull
        assertThat(property.isAssociation).isTrue()
    }

    @Test
    fun `considers transient fields as transient`() {
        val property = entity.getPersistentProperty("transientProp")
        assertThat(property).isNull()
    }

    @Test
    fun `considers embeddable property an assosication`() {
        assertThat(context.getPersistentEntity(SampleEmbeddable::class.java)).isNotNull
    }

    @Test
    fun `does not consider embeddable property assocation`() {
        assertThat(entity.getPersistentProperty("embeddable")!!.isAssociation).isFalse()
    }

    @Test
    fun `does not consider embeddable property an assocation`() {
        assertThat(entity.getPersistentProperty("embedded")!!.isAssociation).isFalse()
    }

    @Test
    fun `detect Requery Version Annotation`() {
        assertThat(getProperty(RequeryVersioned::class.java, "id").isIdProperty).isTrue()

        assertThat(getProperty(SpringDataVersioned::class.java, "version").isVersionProperty).isFalse()
        assertThat(getProperty(RequeryVersioned::class.java, "version").isVersionProperty).isTrue()
    }

    @Test
    fun `considers target entity type for property type`() {

        val property = getProperty(SpecializedAssociation::class.java, "api")

        val entityTypes = property.persistentEntityTypes
        assertThat(entityTypes.iterator().hasNext()).isTrue()
        assertThat(entityTypes.iterator().next()).isEqualTo(ClassTypeInformation.from(Api::class.java))
    }


    private fun getProperty(ownerType: Class<*>, propertyName: String): RequeryPersistentProperty {
        val entity = context.getRequiredPersistentEntity(ownerType)
        return entity.getRequiredPersistentProperty(propertyName)
    }

    class Sample {

        @get:OneToOne
        var other: Sample? = null

        @get:Transient
        var transientProp: String? = null

        var embeddable: SampleEmbeddable? = null

        @get:Embedded
        var embedded: SampleEmbedded? = null
    }

    @Embedded
    class SampleEmbeddable

    class SampleEmbedded

    @org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
    class TypeLevelPropertyAccess {

        var id: String? = null
    }

    class PropertyLevelPropertyAccess {

        var field: String? = null
        @get:org.springframework.data.annotation.AccessType(org.springframework.data.annotation.AccessType.Type.PROPERTY)
        var property: String? = null
    }


    class SpringDataVersioned {

        @Version var version: Long = 0
    }

    class RequeryVersioned {

        @get:Key
        var id: Long? = null

        @io.requery.Version
        var version: Long = 0
    }

    class SpecializedAssociation {

        @get:ManyToOne
        var api: Api? = null
    }

    interface Api

    class Implementation

    class WithIndexed {

        @get:Column(index = true)
        var name: String? = null

        var updatable: String? = null
    }
}