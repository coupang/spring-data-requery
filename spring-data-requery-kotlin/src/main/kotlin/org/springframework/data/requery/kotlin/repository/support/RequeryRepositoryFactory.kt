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

import mu.KotlinLogging
import org.springframework.data.repository.core.EntityInformation
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.query.EvaluationContextProvider
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.repository.query.RequeryQueryLookupStrategy
import java.util.*

/**
 * Requery specific generic repository factory.
 *
 * @author debop
 */
open class RequeryRepositoryFactory(val operations: RequeryOperations) : RepositoryFactorySupport() {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    private val crudMethodMetadataPostProcessor = CrudMethodMetadataPostProcessor()

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        log.trace { "set bean class loader. classLoader=$classLoader" }

        super.setBeanClassLoader(classLoader)
        this.crudMethodMetadataPostProcessor.setBeanClassLoader(classLoader)
    }

    override fun getTargetRepository(metadata: RepositoryInformation): SimpleRequeryRepository<out Any, out Any> {
        val repository = getTargetRepository(metadata, operations)
        repository.setRepositoryMethodMetadata(crudMethodMetadataPostProcessor.getCrudMethodMetadata())
        return repository
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getTargetRepository(information: RepositoryInformation,
                                      operations: RequeryOperations): SimpleRequeryRepository<out Any, out Any> {
        log.debug { "Get target repository. information=$information" }

        val entityInformation = getEntityInformation<Any, Any>(information.domainType as Class<Any>)

        val repository =
            getTargetRepositoryViaReflection(information, entityInformation, operations) as? SimpleRequeryRepository<out Any, out Any>

        log.debug { "find target repository. repository=$repository" }

        return repository!!
    }

    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> = SimpleRequeryRepository::class.java


    override fun getQueryLookupStrategy(key: QueryLookupStrategy.Key?,
                                        evaluationContextProvider: EvaluationContextProvider): Optional<QueryLookupStrategy> {
        log.debug { "Create QueryLookupStrategy by key=$key" }
        return Optional.ofNullable(RequeryQueryLookupStrategy.create(operations, key, evaluationContextProvider))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Any, ID : Any> getEntityInformation(domainClass: Class<E>): EntityInformation<E, ID> {

        return RequeryEntityInformationSupport.getEntityInformation<E, ID>(domainClass.kotlin, operations)
            as RequeryEntityInformationSupport<E, ID>
    }


}