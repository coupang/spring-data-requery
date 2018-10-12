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
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.TargetSource
import org.springframework.aop.framework.ProxyFactory
import org.springframework.aop.interceptor.ExposeInvocationInterceptor
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.ClassUtils
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * [RepositoryProxyPostProcessor] that sets up interceptors to read metadata information from the invoked queryMethod.
 * This is necessary to allow redeclaration of CRUD methods in repository interfaces and configure locking information
 * or query hints on them.
 *
 * @author debop
 */
class CrudMethodMetadataPostProcessor : RepositoryProxyPostProcessor, BeanClassLoaderAware {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    private var classLoader: ClassLoader? = ClassUtils.getDefaultClassLoader()

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        log.trace { "Set class loader for bean. classLoader=$classLoader" }
        this.classLoader = classLoader
    }

    override fun postProcess(factory: ProxyFactory, repositoryInformation: RepositoryInformation) {
        // NOTE: RepositoryInformation#isCustom 이 있네 ...
        log.debug { "Post process for repository. factory=$factory, repository=$repositoryInformation" }

        factory.addAdvice(CrudMethodMetadataPopulatingMethodInterceptor.INSTANCE)
    }

    /**
     * Returns a [CrudMethodMetadata] proxy that will lookup the actual target object by obtaining a thread bound
     * instance from the [TransactionSynchronizationManager] later.
     */
    fun getCrudMethodMetadata(): CrudMethodMetadata {

        val factory = ProxyFactory().apply {
            addInterface(CrudMethodMetadata::class.java)
            targetSource = ThreadBoundTargetSource()
        }
        return factory.getProxy(this.classLoader) as CrudMethodMetadata
    }


    enum class CrudMethodMetadataPopulatingMethodInterceptor : MethodInterceptor {

        INSTANCE;

        companion object {
            private val log = KotlinLogging.logger { }

            private val metadataCache = ConcurrentHashMap<Method, CrudMethodMetadata>()
        }

        override fun invoke(invocation: MethodInvocation): Any {

            val method = invocation.method
            val metadata = TransactionSynchronizationManager.getResource(method) as? CrudMethodMetadata
            log.trace { "Invoke method ... method=$method, metadata=$metadata" }

            if(metadata != null) {
                return invocation.proceed()
            }

            val methodMetadata = metadataCache.computeIfAbsent(method) { DefaultCrudMethodMetadata(it) }
            log.trace { "Bind to transaction manager. method=$method, methodMetadata=$methodMetadata" }

            TransactionSynchronizationManager.bindResource(method, methodMetadata)

            return try {
                invocation.proceed()
            } finally {
                log.trace { "Unbind from transaction manager. method=$method" }
                TransactionSynchronizationManager.unbindResource(method)
            }
        }
    }


    private class DefaultCrudMethodMetadata(override val method: Method) : CrudMethodMetadata

    private class ThreadBoundTargetSource : TargetSource {

        companion object {
            private val log = KotlinLogging.logger { }
        }

        override fun getTargetClass(): Class<*> = CrudMethodMetadata::class.java

        override fun isStatic(): Boolean = false

        override fun getTarget(): Any? {
            val invocation = ExposeInvocationInterceptor.currentInvocation()
            return TransactionSynchronizationManager.getResource(invocation.method)
        }

        override fun releaseTarget(target: Any) {
            // Nothing to do
        }
    }
}