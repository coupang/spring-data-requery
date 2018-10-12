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

import mu.KotlinLogging
import org.springframework.data.mapping.Association
import org.springframework.data.mapping.PersistentEntity
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty
import org.springframework.data.mapping.model.FieldNamingStrategy
import org.springframework.data.mapping.model.Property
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy
import org.springframework.data.mapping.model.SimpleTypeHolder

/**
 * Default implementation of [RequeryPersistentProperty]
 *
 * @author debop
 */
open class DefaultRequeryPersistentProperty @JvmOverloads constructor(
    property: Property,
    owner: PersistentEntity<*, RequeryPersistentProperty>,
    simpleTypeHolder: SimpleTypeHolder,
    val fieldNamingStrategy: FieldNamingStrategy = PropertyNameFieldNamingStrategy.INSTANCE
) : AnnotationBasedPersistentProperty<RequeryPersistentProperty>(property, owner, simpleTypeHolder),
    RequeryPersistentProperty {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    open override fun createAssociation(): Association<RequeryPersistentProperty> = Association(this, null)

    open override fun isIdProperty(): Boolean = findAnnotation(io.requery.Key::class.java) != null

    open override fun isAssociation(): Boolean =
        findAnnotation(io.requery.OneToOne::class.java) != null ||
        findAnnotation(io.requery.OneToMany::class.java) != null ||
        findAnnotation(io.requery.ManyToOne::class.java) != null ||
        findAnnotation(io.requery.ManyToMany::class.java) != null

    open override fun isEmbedded(): Boolean = findAnnotation(io.requery.Embedded::class.java) != null

    open override fun hasIndex(): Boolean = findAnnotation(io.requery.Index::class.java) != null

    open override fun isTransient(): Boolean = findAnnotation(io.requery.Transient::class.java) != null

    open override fun isVersionProperty(): Boolean = findAnnotation(io.requery.Version::class.java) != null

    open override fun getFieldName(): String {
        return when {
            isIdProperty -> property.name
            else -> getAnnotatedColumnName() ?: fieldNamingStrategy.getFieldName(this)
        }
    }


    private fun getAnnotatedColumnName(): String? {

        return findAnnotation(io.requery.Column::class.java)?.let {
            when {
                it.value.isBlank() -> null
                else -> it.value
            }
        }
    }
}