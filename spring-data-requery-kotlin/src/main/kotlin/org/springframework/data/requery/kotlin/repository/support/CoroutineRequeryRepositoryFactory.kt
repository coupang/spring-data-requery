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

import mu.KLogging
import org.springframework.data.repository.core.EntityInformation
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryLookupStrategy.Key
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.repository.query.CoroutineRequeryQueryLookupStrategy
import java.util.*

/**
 * Requery with coroutine specific generic repository factory.
 *
 * @author debop (Sunghyouk Bae)
 */
open class CoroutineRequeryRepositoryFactory(private val operations: CoroutineRequeryOperations) : RepositoryFactorySupport() {

    companion object : KLogging()

    private val crudMethodMetadataPostProcessor = CrudMethodMetadataPostProcessor()

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        logger.trace { "Set bean classLoader. classLoader=$classLoader" }

        super.setBeanClassLoader(classLoader)
        crudMethodMetadataPostProcessor.setBeanClassLoader(classLoader)
    }

    override fun getTargetRepository(metadata: RepositoryInformation): SimpleCoroutineRequeryRepository<out Any, out Any> {
        val repository = getTargetRepository(metadata, operations)
        repository.setRepositoryMethodMetadata(crudMethodMetadataPostProcessor.getCrudMethodMetadata())
        return repository
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getTargetRepository(information: RepositoryInformation,
                                      operations: CoroutineRequeryOperations): SimpleCoroutineRequeryRepository<out Any, out Any> {
        logger.debug { "Get target coroutine repository for domain type=${information.domainType.name}" }

        val entityInformation = getEntityInformation<Any, Any>(information.domainType as Class<Any>)

        val repository =
            getTargetRepositoryViaReflection(information,
                                             entityInformation,
                                             operations) as? SimpleCoroutineRequeryRepository<out Any, out Any>

        check(repository != null) { "Fail to find target repository for domain type=[${information.domainType.name}]" }

        logger.debug { "Find target repository. repository=$repository" }
        return repository!!
    }

    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> =
        SimpleCoroutineRequeryRepository::class.java

    override fun getQueryLookupStrategy(key: Key?,
                                        evaluationContextProvider: QueryMethodEvaluationContextProvider): Optional<QueryLookupStrategy> {
        logger.debug { "Create CoroutineQueryLookupStrategy by key=$key" }
        return Optional.ofNullable(CoroutineRequeryQueryLookupStrategy.create(operations, key, evaluationContextProvider))
    }

    override fun <E : Any, ID : Any> getEntityInformation(domainClass: Class<E>): EntityInformation<E, ID> {
        return RequeryEntityInformationSupport.getEntityInformation(domainClass.kotlin, operations.entityModel)
    }
}