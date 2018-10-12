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

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.repository.config.DefaultRepositoryBaseClass
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.requery.kotlin.repository.support.RequeryRepositoryFactoryBean
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * org.springframework.data.requery.repository.config.EnableRequeryRepositories
 *
 * @author debop
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Import(RequeryRepositoriesRegistar::class)
annotation class EnableRequeryRepositories(

    vararg val value: String = [],

    val basePackages: Array<String> = [],

    val basePackageClasses: Array<KClass<*>> = [],

    val includeFilters: Array<ComponentScan.Filter> = [],

    val excludeFilters: Array<ComponentScan.Filter> = [],

    val repositoryImplementationPostfix: String = "Impl",

    // Not supported 
    val namedQueriesLocation: String = "",

    val queryLookupStrategy: QueryLookupStrategy.Key = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND,

    val repositoryFactoryBeanClass: KClass<*> = RequeryRepositoryFactoryBean::class,

    val repositoryBaseClass: KClass<*> = DefaultRepositoryBaseClass::class,

    val transactionManagerRef: String = "transactionManager",

    val considerNestedRepositories: Boolean = false,

    val enableDefaultTransactions: Boolean = true
)
