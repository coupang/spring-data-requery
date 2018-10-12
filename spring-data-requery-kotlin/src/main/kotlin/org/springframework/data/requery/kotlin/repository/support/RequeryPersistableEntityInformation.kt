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

package org.springframework.data.requery.kotlin.repository.support

import io.requery.meta.EntityModel
import mu.KotlinLogging
import org.springframework.data.domain.Persistable
import kotlin.reflect.KClass

/**
 * org.springframework.data.requery.repository.support.RequeryPersistableEntityInformation
 *
 * @author debop
 */
class RequeryPersistableEntityInformation<E : Any, ID : Any>(kotlinType: KClass<E>, entityModel: EntityModel)
    : RequeryEntityModelEntityInformation<E, ID>(kotlinType, entityModel) {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    init {
        log.debug { "Create RequeryPersistableEntityInformation. kotlinType=$kotlinType, entityModel=${entityModel.name}" }
    }

    override fun isNew(entity: E): Boolean {
        log.trace { "Is new entity?. entity=$entity" }

        return when(entity) {
            is Persistable<*> -> entity.isNew
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getId(entity: E): ID? {
        log.trace { "Get identifier of entity. entity=$entity" }
        return when(entity) {
            is Persistable<*> -> entity.id as? ID
            else -> null
        }
    }

}