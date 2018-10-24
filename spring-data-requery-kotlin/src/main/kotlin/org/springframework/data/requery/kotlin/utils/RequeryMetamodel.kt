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
 * Requery 가 관리하는 entity들의 메타정보를 제공합니다.
 *
 * @property entityModel 관리하는 엔티티들의 정보를 담은 [EntityModel]
 * @author debop
 */
class RequeryMetamodel(val entityModel: EntityModel) {

    private val managedTypes: MutableList<Class<out Any>> by lazy {
        ArrayList<Class<out Any>>().apply {
            addAll(entityModel.types.mapNotNull { it.classType })
        }
    }

    /**
     * 지정한 entityClass 가 Requery의 EntityModel에 등록된 수형인지 여부
     * @param entityClass entity type
     */
    fun isRequeryManaged(entityClass: Class<*>): Boolean {
        return managedTypes.contains(entityClass)
    }

    /**
     * 지정한 entityClass의 수형이 하나의 identitifer만 가지는지 여부
     * @param entityClass
     * @param name
     * @param attributeClass
     */
    fun isSingleIdAttribute(entityClass: Class<*>, name: String, attributeClass: Class<*>): Boolean {

        return entityModel.types
            .asSequence()
            .filter { type ->
                type.name == name &&
                type.classType == entityClass &&
                type.singleKeyAttribute != null &&
                type.classType == attributeClass
            }
            .any()
    }
}