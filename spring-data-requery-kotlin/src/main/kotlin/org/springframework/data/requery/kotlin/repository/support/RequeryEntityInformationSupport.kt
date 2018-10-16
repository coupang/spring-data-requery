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
import org.springframework.data.repository.core.support.AbstractEntityInformation
import org.springframework.data.requery.kotlin.repository.query.DefaultRequeryEntityMetadata
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * org.springframework.data.requery.repository.support.RequeryEntityInformationSupport
 *
 * @author debop
 */
abstract class RequeryEntityInformationSupport<E : Any, ID : Any>(final override val kotlinType: KClass<E>)
    : AbstractEntityInformation<E, ID>(kotlinType.java), RequeryEntityInformation<E, ID> {

    companion object {
        private val log = KotlinLogging.logger { }

        @JvmStatic
        fun <E : Any, ID : Any> getEntityInformation(domainKlass: KClass<E>,
                                                     entityModel: EntityModel): RequeryEntityInformation<E, ID> {

            log.debug { "domainClass=$domainKlass, entityModel=$entityModel" }


            //return if(Persistable::class.java.isAssignableFrom(kotlinType.java)) {
            return if(Persistable::class.isSuperclassOf(domainKlass)) {
                RequeryPersistableEntityInformation(domainKlass, entityModel)
            } else {
                RequeryEntityModelEntityInformation(domainKlass, entityModel)
            }
        }
    }

    private val metadata = DefaultRequeryEntityMetadata.of(kotlinType)

    override val entityName: String get() = metadata.entityName

    override val modelName: String get() = metadata.modelName

}