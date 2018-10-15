package org.springframework.data.requery.kotlin.repository.support

import mu.KLogging
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.query.EvaluationContextProvider
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryLookupStrategy.Key
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations
import org.springframework.data.requery.kotlin.repository.query.RequeryQueryLookupStrategy
import java.util.*

/**
 * [CoroutineRequeryRepository] 를 생성하는 Factory 입니다.
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
        logger.debug { "Get target repository for domain type=${information.domainType}" }

        val entityInformation = getEntityInformation<Any, Any>(information.domainType as Class<Any>)

        val repository =
            getTargetRepositoryViaReflection(information,
                                             entityInformation,
                                             operations) as? SimpleCoroutineRequeryRepository<out Any, out Any>

        logger.debug { "Find target repository. repository=$repository" }
        return repository!!
    }

    override fun getQueryLookupStrategy(key: Key?,
                                        evaluationContextProvider: EvaluationContextProvider): Optional<QueryLookupStrategy> {
        logger.debug { "Create QueryLookupStrategy by key=$key" }
        return Optional.ofNullable(RequeryQueryLookupStrategy.create(operations, key, evaluationContextProvider))
    }
}