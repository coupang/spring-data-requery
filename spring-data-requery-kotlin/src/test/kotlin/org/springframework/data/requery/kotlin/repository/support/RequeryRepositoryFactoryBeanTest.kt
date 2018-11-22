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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import io.requery.meta.EntityModel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.dao.support.PersistenceExceptionTranslator
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.EntityInformation
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryComposition
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.domain.sample.User
import org.springframework.data.requery.kotlin.repository.RequeryRepository
import org.springframework.data.requery.kotlin.uninitialized

class RequeryRepositoryFactoryBeanTest {

    private lateinit var factoryBean: RequeryRepositoryFactoryBean<SimpleSampleRepository, User, Int>
    private lateinit var factory: StubRepositoryFactorySupport

    val operations = mock<RequeryOperations>()
    val beanFactory = mock<ListableBeanFactory>()
    val translator = mock<PersistenceExceptionTranslator>()
    val repository = mock<Repository<out Any, out Any>>()
    val entityModel = mock<EntityModel>()

    @Before
    fun setup() {
        val beans = hashMapOf("foo" to translator)


        whenever(beanFactory.getBeansOfType(eq(PersistenceExceptionTranslator::class.java), any(), any()))
            .doReturn(beans)

        whenever(operations.entityModel).doReturn(entityModel)

        factory = spy(StubRepositoryFactorySupport(repository))

        // Setup standard factory configuration
        factoryBean = DummyRequeryRepositoryFactoryBean(SimpleSampleRepository::class.java)
        factoryBean.setOperations(operations)
    }

    @Test
    fun `set basic instance correctly`() {

        factoryBean.setBeanFactory(beanFactory)
        factoryBean.afterPropertiesSet()

        assertThat(factoryBean.`object`).isNotNull
    }

    @Test
    fun `requires listable bean factory`() {
        assertThatThrownBy {
            factoryBean.setBeanFactory(mock())
        }.isInstanceOf(IllegalArgumentException::class.java)
    }


    private inner class DummyRequeryRepositoryFactoryBean<T : RequeryRepository<E, ID>, E : Any, ID : Any>(repositoryInterface: Class<out T>)
        : RequeryRepositoryFactoryBean<T, E, ID>(repositoryInterface) {

        override fun doCreateRepositoryFactory(): RepositoryFactorySupport {
            return factory
        }
    }

    private interface SimpleSampleRepository : RequeryRepository<User, Int>

    @Suppress("UNCHECKED_CAST")
    private class StubRepositoryFactorySupport constructor(private val repository: Repository<*, *>) : RepositoryFactorySupport() {

        override fun <T> getRepository(repositoryInterface: Class<T>, fragments: RepositoryComposition.RepositoryFragments): T {
            return repository as T
        }

        override fun <T, ID> getEntityInformation(domainClass: Class<T>): EntityInformation<T, ID> = uninitialized()

        override fun getTargetRepository(metadata: RepositoryInformation): Any = uninitialized()

        override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> = uninitialized()
    }
}