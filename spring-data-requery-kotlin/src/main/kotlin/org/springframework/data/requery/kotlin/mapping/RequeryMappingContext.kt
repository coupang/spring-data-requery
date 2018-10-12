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
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.mapping.context.AbstractMappingContext
import org.springframework.data.mapping.model.FieldNamingStrategy
import org.springframework.data.mapping.model.Property
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy
import org.springframework.data.mapping.model.SimpleTypeHolder
import org.springframework.data.util.TypeInformation

/**
 * RequeryMappingContext
 *
 * @author debop
 * @since 18. 7. 2
 */
open class RequeryMappingContext : AbstractMappingContext<DefaultRequeryPersistentEntity<*>, RequeryPersistentProperty>(),
                                   ApplicationContextAware {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    private var fieldNamingStrategy: FieldNamingStrategy? = PropertyNameFieldNamingStrategy.INSTANCE
    private var applicationContext: ApplicationContext? = null

    open fun setFieldNamingStrategy(fieldNamingStrategy: FieldNamingStrategy) {
        this.fieldNamingStrategy = fieldNamingStrategy
    }

    open override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    open override fun <T> createPersistentEntity(typeInformation: TypeInformation<T>): DefaultRequeryPersistentEntity<*> {

        return DefaultRequeryPersistentEntity(typeInformation)
            .apply {
                log.debug { "Create persistent entity. typeInformation=$typeInformation" }
                applicationContext?.let { this.setApplicationContext(it) }
            }
    }

    open override fun createPersistentProperty(property: Property,
                                               owner: DefaultRequeryPersistentEntity<*>,
                                               simpleTypeHolder: SimpleTypeHolder): RequeryPersistentProperty {
        log.debug { "Create property. property=$property" }

        return DefaultRequeryPersistentProperty(property,
                                                owner,
                                                simpleTypeHolder,
                                                fieldNamingStrategy ?: PropertyNameFieldNamingStrategy.INSTANCE)
    }
}
