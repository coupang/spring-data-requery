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

import io.requery.meta.Attribute
import io.requery.meta.EntityModel
import io.requery.meta.Type
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.BeanWrapper
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper
import kotlin.reflect.KClass

/**
 * org.springframework.data.requery.repository.support.RequeryEntityModelEntityInformation
 *
 * @author debop
 */
open class RequeryEntityModelEntityInformation<E : Any, ID : Any>(kotlinType: KClass<E>,
                                                                  val entityModel: EntityModel)
    : RequeryEntityInformationSupport<E, ID>(kotlinType) {

    companion object {
        private val log: KLogger by lazy { KotlinLogging.logger {} }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        private fun <E> findVersionAttribute(domainType: Type<E>, entityModel: EntityModel): Attribute<out E, out Any>? {

            log.debug { "Find version attribute. domainType=$domainType, entityModel=${entityModel.name}" }

            val attr = domainType.attributes.find { it.isVersion }
            if(attr != null) {
                return attr
            }

            val baseClass = domainType.baseType
            try {
                val baseType = entityModel.typeOf(baseClass)
                if(baseType.keyAttributes.isNotEmpty()) {
                    return null
                }
                return findVersionAttribute(baseType as Type<E>, entityModel)
            } catch(e: IllegalArgumentException) {
                return null
            }
        }
    }

    private val idMetadata: IdMetadata<E>
    private val versionAttribute: Attribute<out E, out Any>?
    private val _entityName: String?

    override val entityName: String
        get() = _entityName ?: super.entityName

    init {
        log.debug { "Create RequeryEntityModelEntityInformation. kotinType=$kotlinType, entityModel=${entityModel.name}" }

        val domainType = entityModel.typeOf(kotlinType.java)
                         ?: throw IllegalArgumentException("The given domain class can not be found in the given EntityModel! " +
                                                           "kotlinType=$kotlinType, entityModel=${entityModel.name}")

        this._entityName = domainType.name

        if(domainType.keyAttributes.isEmpty()) {
            throw IllegalArgumentException("The given domain class does not contains an id attribute! kotlinType=$kotlinType")
        }

        this.idMetadata = IdMetadata(domainType)
        this.versionAttribute = findVersionAttribute(domainType, entityModel)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getId(entity: E): ID? {
        log.trace { "Get identifier value. entity=$entity" }

        val entityWrapper = DirectFieldAccessFallbackBeanWrapper(entity)

        if(idMetadata.hasSimpleId) {
            return entityWrapper.getPropertyValue(idMetadata.simpleIdAttribute.name) as? ID
        }

        val idWrapper = IdentifierDerivingDirectFieldAccessFallbackBeanWrapper(idMetadata.getType().kotlin, entityModel)
        var partialValueFound = false

        idMetadata.forEach {
            val propertyValue = entityWrapper.getPropertyValue(it.name)
            if(propertyValue != null) {
                partialValueFound = true
            }
            log.trace { "Set property value. attribute name=${it.name}, propertyValue=$propertyValue" }
            idWrapper.setPropertyValue(it.name, propertyValue)
        }
        log.trace { "partialValueFound=$partialValueFound" }

        return if(partialValueFound) idWrapper.wrappedInstance as ID else null
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIdType(): Class<ID> {
        return idMetadata.getType() as Class<ID>
    }

    override fun getIdAttribute(): Attribute<out E, out Any>? {
        return idMetadata.simpleIdAttribute
    }

    override fun hasCompositeId(): Boolean {
        return !idMetadata.hasSimpleId
    }

    override fun getIdAttributeNames(): Iterable<String> {
        return idMetadata.attributes.map { it.name }
    }

    override fun getCompositeIdAttributeValue(id: Any, idAttribute: String): Any? {
        check(hasCompositeId()) { "Entity must have a composite id!, id=$id, idAttribute=$idAttribute" }

        return DirectFieldAccessFallbackBeanWrapper(id)
            .getPropertyValue(idAttribute)
    }

    override fun isNew(entity: E): Boolean {
        log.trace { "is new entity? ... entity=$entity" }

        if(versionAttribute == null || versionAttribute.classType.isPrimitive) {
            return super.isNew(entity)
        }

        val wrapper = DirectFieldAccessFallbackBeanWrapper(entity)

        return wrapper.getPropertyValue(versionAttribute.name) == null
    }

    private class IdMetadata<E>(val domainType: Type<E>) : Iterable<Attribute<out E, out Any>> {

        val attributes: Set<Attribute<E, out Any>> = when(domainType.keyAttributes.size) {
            1 -> setOf<Attribute<E, out Any>>(domainType.singleKeyAttribute)
            else -> domainType.keyAttributes.toSet()
        }

        private val idType: Class<out Any> by lazy {
            tryExtractIdTypeWithFallbackToIdTypeLookup()
            ?: throw IllegalArgumentException("Cannot resolve Key type from $domainType")
        }

        val hasSimpleId: Boolean get() = attributes.size == 1

        fun getType(): Class<out Any> {
            return idType
        }

        private fun tryExtractIdTypeWithFallbackToIdTypeLookup(): Class<out Any>? {
            return try {
                domainType.singleKeyAttribute.classType
            } catch(e: IllegalStateException) {
                null
            }
        }

        val simpleIdAttribute: Attribute<out E, out Any>
            get() = attributes.first()

        override fun iterator(): Iterator<Attribute<out E, out Any>> {
            return attributes.iterator()
        }
    }

    private class IdentifierDerivingDirectFieldAccessFallbackBeanWrapper(kotlinType: KClass<out Any>,
                                                                         val entityModel: EntityModel)
        : DirectFieldAccessFallbackBeanWrapper(kotlinType.java) {

        companion object {
            private val log = KotlinLogging.logger { }
        }

        val requeryMetamodel = RequeryMetamodel(entityModel)

        /**
         * In addition to the functionality described in {@link BeanWrapperImpl} it is checked whether we have a nested
         * entity that is part of the id key. If this is the case, we need to derive the identifier of the nested entity.
         */
        override fun setPropertyValue(propertyName: String, value: Any?) {

            if(!isIdentifierDerivationNecessary(value)) {
                super.setPropertyValue(propertyName, value)
                return
            }

            // Derive the identifier from the nested entity that is part of the composite key.
            val nestedEntityInformation = RequeryEntityModelEntityInformation<Any, Any>(Any::class, this.entityModel)

            val nestedIdPropertyValue = DirectFieldAccessFallbackBeanWrapper(value!!)
                .getPropertyValue(nestedEntityInformation.getRequiredIdAttribute().name)

            super.setPropertyValue(propertyName, nestedIdPropertyValue)
        }

        private fun extractActualIdPropertyValue(sourceIdValueWrapper: BeanWrapper,
                                                 idAttributeName: String): Any? {
            val idPropertyValue = sourceIdValueWrapper.getPropertyValue(idAttributeName)

            return idPropertyValue?.let {
                val idPropertyValueClass = idPropertyValue.javaClass


                when {
                    requeryMetamodel.isRequeryManaged(idPropertyValueClass) ->
                        idPropertyValue
                    else -> DirectFieldAccessFallbackBeanWrapper(idPropertyValueClass)
                        .getPropertyValue(tryFindSingularIdAttributeNameOrUseFallback(idPropertyValueClass, idAttributeName))
                }
            }
        }

        private fun tryFindSingularIdAttributeNameOrUseFallback(idPropertyValueType: Class<out Any>,
                                                                fallbackIdTypePropertyName: String): String {
            log.debug { "idPropertyValue=$idPropertyValueType, fallbackIdTypePropertyName=$fallbackIdTypePropertyName" }

            val idPropertyType = entityModel.typeOf(idPropertyValueType)

            return idPropertyType?.attributes
                       ?.find { it.isKey }
                       ?.name
                   ?: fallbackIdTypePropertyName
        }

        private fun isIdentifierDerivationNecessary(value: Any?): Boolean {
            return value?.let {
                return try {
                    val entityType = this.entityModel.typeOf(it.javaClass)
                    entityType != null && !entityType.keyAttributes.isEmpty()
                } catch(e: IllegalArgumentException) {
                    false
                }
            } ?: false
        }
    }
}