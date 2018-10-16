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

package org.springframework.data.requery.kotlin.repository.config

import mu.KLogging
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.core.io.ResourceLoader
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport
import org.springframework.data.repository.config.RepositoryConfigurationSource
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource
import org.springframework.data.requery.kotlin.repository.RequeryRepository
import org.springframework.data.requery.kotlin.repository.support.RequeryRepositoryFactoryBean
import org.springframework.util.ClassUtils
import java.util.*

/**
 * org.springframework.data.requery.repository.config.RequeryRepositoryConfigurationExtension
 *
 * @author debop
 */
class RequeryRepositoryConfigurationExtension : RepositoryConfigurationExtensionSupport() {

    companion object : KLogging() {
        private const val DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager"
        private const val ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE = "enableDefaultTransactions"
    }

    override fun getModuleName(): String = "REQUERY"

    override fun getRepositoryFactoryBeanClassName(): String =
        RequeryRepositoryFactoryBean::class.java.name

    override fun getModulePrefix(): String = moduleName.toLowerCase(Locale.US)

    override fun getIdentifyingAnnotations(): MutableCollection<Class<out Annotation>> =
        mutableListOf(io.requery.Entity::class.java,
                      io.requery.Superclass::class.java)

    override fun getIdentifyingTypes(): MutableCollection<Class<*>> =
        mutableListOf(RequeryRepository::class.java)


    override fun postProcess(builder: BeanDefinitionBuilder, source: RepositoryConfigurationSource) {

        val transactionManagerRef = source.getAttribute("transactionManagerRef")

        builder.addPropertyValue(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME,
                                 transactionManagerRef.orElse(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME))
    }

    override fun postProcess(builder: BeanDefinitionBuilder, config: AnnotationRepositoryConfigurationSource) {

        val attributes = config.attributes

        builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE,
                                 attributes.getBoolean(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE))
    }

    override fun postProcess(builder: BeanDefinitionBuilder, config: XmlRepositoryConfigurationSource) {

        val enableDefaultTransactions = config.getAttribute(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE)

        if(enableDefaultTransactions.isPresent && enableDefaultTransactions.get().isNotBlank()) {
            builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE,
                                     enableDefaultTransactions.get())
        }
    }

    override fun getConfigurationInspectionClassLoader(loader: ResourceLoader): ClassLoader? {

        //        return if(loader.classLoader != null && LazyJvmAgent.isActive(loader.classLoader))
        //            InspectionClassLoader(loader.classLoader!!)
        //        else loader.classLoader

        return loader.classLoader?.let {
            InspectionClassLoader(it)
        } ?: loader.classLoader
    }


    object LazyJvmAgent {

        private val AGENT_CLASSES: Set<String> by lazy {
            LinkedHashSet<String>().apply {
                add("org.springframework.instrument.InstrumentationSavingAgent")
                // add("org.eclipse.persistence.internal.jpa.deployment.JavaSECMPInitializerAgent")
            }
        }

        fun isActive(classLoader: ClassLoader?): Boolean =
            AGENT_CLASSES.any { ClassUtils.isPresent(it, classLoader) }
    }
}