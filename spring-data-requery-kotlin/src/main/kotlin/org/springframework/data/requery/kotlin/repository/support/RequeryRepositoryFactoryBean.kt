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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mapping.context.MappingContext
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport
import org.springframework.data.requery.kotlin.core.RequeryOperations

/**
 * Special adapter for Springs [org.springframework.beans.factory.FactoryBean] interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author debop
 */
open class RequeryRepositoryFactoryBean<T : Repository<E, ID>, E, ID>(repositoryInterface: Class<out T>)
    : TransactionalRepositoryFactoryBeanSupport<T, E, ID>(repositoryInterface) {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    private var operations: RequeryOperations? = null

    @Autowired(required = false)
    open fun setOperations(operations: RequeryOperations) {
        this.operations = operations
    }

    open override fun setMappingContext(mappingContext: MappingContext<*, *>) {
        super.setMappingContext(mappingContext)
    }

    open override fun doCreateRepositoryFactory(): RepositoryFactorySupport {
        require(operations != null) { "RequeryOperations must not be null!" }
        return createRepositoryFactory(operations!!)
    }

    protected open fun createRepositoryFactory(operations: RequeryOperations): RequeryRepositoryFactory {
        return RequeryRepositoryFactory(operations)
    }

    open override fun afterPropertiesSet() {
        require(operations != null) { "RequeryOperations must not be null!" }

        log.debug { "Before afterPropertiesSet" }
        try {
            super.afterPropertiesSet()
        } catch(e: Exception) {
            log.error(e) { "Fail to run afterPropertiesSet" }
        }
        log.debug { "After afterPropertiesSet" }
    }
}