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

import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.mapping.model.BasicPersistentEntity
import org.springframework.data.util.TypeInformation

/**
 * org.springframework.data.requery.mapping.DefaultRequeryPersistentEntity
 *
 * @author debop
 */
class DefaultRequeryPersistentEntity<E>(information: TypeInformation<E>)
    : BasicPersistentEntity<E, RequeryPersistentProperty>(information), RequeryPersistentEntity<E> {

    private val name: String

    override val idProperties: MutableList<RequeryPersistentProperty> = mutableListOf()
    override val embeddedProperties: MutableList<RequeryPersistentProperty> = mutableListOf()
    override val indexes: MutableList<RequeryPersistentProperty> = mutableListOf()

    override val singleIdProperty: RequeryPersistentProperty?
        get() = idProperties.firstOrNull()

    private val annotationCache: MutableMap<Class<out Annotation>, Annotation?> = mutableMapOf()
    private val repeatableAnnotationCache: MutableMap<Class<out Annotation>, Set<Annotation>> = mutableMapOf()

    init {
        name = findAnnotation(io.requery.Entity::class.java)?.let { entity ->
            if(entity.name.isNotBlank()) entity.name else information.type.simpleName
        } ?: information.type.simpleName
    }

    override fun getName(): String {
        return name
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        // Nothing to do.
    }

    override fun addPersistentProperty(property: RequeryPersistentProperty) {
        super.addPersistentProperty(property)

        if(property.isIdProperty) {
            idProperties.add(property)
        }
        if(property.isEmbedded()) {
            embeddedProperties.add(property)
        }
        if(property.hasIndex()) {
            indexes.add(property)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation> findAnnotation(annotationType: Class<A>): A? {
        return annotationCache.computeIfAbsent(annotationType) {
            AnnotatedElementUtils.findMergedAnnotation<A>(type, annotationType)
        } as? A
    }

    @Suppress("UNCHECKED_CAST")
    fun <A : Annotation> findAnnotations(annotationType: Class<A>): Set<A> {
        return repeatableAnnotationCache.computeIfAbsent(annotationType) {
            AnnotatedElementUtils.findMergedRepeatableAnnotations<A>(type, annotationType)
        } as Set<A>
    }
}