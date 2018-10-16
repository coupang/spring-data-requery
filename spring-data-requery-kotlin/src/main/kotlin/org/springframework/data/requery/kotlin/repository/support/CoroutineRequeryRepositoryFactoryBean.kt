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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations

/**
 * Special adapter for Springs [org.springframework.beans.factory.FactoryBean] interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author debop (Sunghyouk Bae)
 * @since 18. 10. 16
 */
open class CoroutineRequeryRepositoryFactoryBean<T : Repository<E, ID>, E : Any, ID : Any>(repositoryInterface: Class<out T>)
    : TransactionalRepositoryFactoryBeanSupport<T, E, ID>(repositoryInterface) {

    companion object : KLogging()

    private var operations: CoroutineRequeryOperations? = null

    @Autowired(required = false)
    open fun setOperations(operations: CoroutineRequeryOperations) {
        this.operations = operations
    }

    override fun doCreateRepositoryFactory(): RepositoryFactorySupport {
        require(operations != null) { "operation must not be null." }
        return createRepositoryFactory(operations!!)
    }

    protected open fun createRepositoryFactory(operations: CoroutineRequeryOperations): CoroutineRequeryRepositoryFactory {
        logger.debug { "Create CoroutineRequeryRepositoryFactory." }
        return CoroutineRequeryRepositoryFactory(operations)
    }

    open override fun afterPropertiesSet() {
        require(operations != null) { "operation must not be null." }

        logger.debug { "Begin afterPropertiesSet in CoroutineRequeryRepositoryFactoryBean" }
        super.afterPropertiesSet()
    }
}