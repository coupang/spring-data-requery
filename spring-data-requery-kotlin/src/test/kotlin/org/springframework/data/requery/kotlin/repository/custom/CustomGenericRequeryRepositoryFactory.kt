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

package org.springframework.data.requery.kotlin.repository.custom

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.repository.support.RequeryEntityInformation
import org.springframework.data.requery.kotlin.repository.support.RequeryRepositoryFactory
import org.springframework.data.requery.kotlin.repository.support.SimpleRequeryRepository

/**
 * org.springframework.data.requery.repository.custom.CustomGenericRequeryRepositoryFactory
 *
 * @author debop
 */
class CustomGenericRequeryRepositoryFactory(operations: RequeryOperations) : RequeryRepositoryFactory(operations) {

    override fun getTargetRepository(metadata: RepositoryInformation): SimpleRequeryRepository<out Any, *> {

        val entityMetadata = mock<RequeryEntityInformation<out Any, out Any>>()

        whenever(entityMetadata.kotlinType).doReturn(metadata.domainType.kotlin)
        whenever(entityMetadata.javaType).doReturn(metadata.domainType)

        return CustomGenericRequeryRepository(entityMetadata, operations)
    }

    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
        return CustomGenericRequeryRepository::class.java
    }
}