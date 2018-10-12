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

package org.springframework.data.requery.kotlin.utils

import io.requery.meta.EntityModel

/**
 * RequeryMetamodel
 *
 * @author debop
 */
class RequeryMetamodel(val entityModel: EntityModel) {

    private val managedTypes = arrayListOf<Class<*>>()

    fun isRequeryManaged(entityClass: Class<*>): Boolean {
        return getManagedTypes().contains(entityClass)
    }

    fun isSingleIdAttribute(entityClass: Class<*>, name: String, attributeClass: Class<*>): Boolean {

        return entityModel.types
                   .filter { it.classType == entityClass }
                   .mapNotNull { it.singleKeyAttribute }
                   .filter { it.classType == attributeClass }
                   .map { it.name == name }
                   .firstOrNull()
               ?: false

    }

    private fun getManagedTypes(): Collection<Class<*>> {
        if(managedTypes.isEmpty()) {
            managedTypes.addAll(entityModel.types.mapNotNull { it.classType })
        }
        return managedTypes
    }

}