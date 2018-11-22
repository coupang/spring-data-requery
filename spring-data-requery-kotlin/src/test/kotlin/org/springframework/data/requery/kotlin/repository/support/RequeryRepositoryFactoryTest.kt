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

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.requery.meta.EntityModel
import io.requery.sql.KotlinEntityDataStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.data.repository.core.EntityInformation
import org.springframework.data.repository.core.support.RepositoryComposition
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.domain.sample.User
import org.springframework.data.requery.kotlin.domain.sample.UserEntity
import org.springframework.data.requery.kotlin.repository.RequeryRepository
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.*

/**
 * RequeryRepositoryFactoryTest
 *
 * @author debop
 */
class RequeryRepositoryFactoryTest {

    lateinit var factory: RequeryRepositoryFactory

    val operations = mock<RequeryOperations>()
    val entityModel = mock<EntityModel>()
    val dataStore = mock<KotlinEntityDataStore<Any>>()
    val entityInformation = mock<RequeryEntityInformation<User, Int>>()

    @Before
    fun setup() {
        whenever(operations.dataStore).doReturn(dataStore)
        whenever(operations.entityModel).doReturn(entityModel)

        whenever(entityInformation.javaType).doReturn(User::class.java)

        factory = object : RequeryRepositoryFactory(operations) {
            @Suppress("UNCHECKED_CAST")
            override fun <E : Any, ID : Any> getEntityInformation(domainClass: Class<E>): EntityInformation<E, ID> {
                return entityInformation as RequeryEntityInformation<E, ID>
            }
        }

        factory.setQueryLookupStrategyKey(QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND)
    }

    @Test
    fun `sutup basic instance correctly`() {
        assertThat(factory.getRepository(SimpleSampleRepository::class.java)).isNotNull
    }

    @Test
    fun `allow calling of object methods`() {

        val repository = factory.getRepository(SimpleSampleRepository::class.java)

        assertThat(repository).isNotNull
        assertThat(repository.hashCode()).isNotEqualTo(0)
        assertThat(repository.toString()).isNotEmpty()
        assertThat(Objects.equals(repository, repository)).isTrue()
    }

    @Ignore("Named parameter는 지원하지 않습니다.")
    @Test
    fun `captures missing custom implementation and provides interface name`() {
        try {
            factory.getRepository(SampleRepository::class.java)
        } catch(e: IllegalArgumentException) {
            assertThat(e.message).contains(SampleRepository::class.java.name)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handle runtime exception`() {

        val fragments = RepositoryComposition.RepositoryFragments.just(SampleCustomRepositoryImpl())
        val repository = factory.getRepository(SampleRepository::class.java, fragments)

        repository.throwingRuntimeException()
    }

    @Ignore("Kotlin not null 때문에 mocking을 모두 해야 한다.")
    @Test(expected = IOException::class)
    fun `handle checked exception`() {

        val fragments = RepositoryComposition.RepositoryFragments.just(SampleCustomRepositoryImpl())
        val repository = factory.getRepository(SampleRepository::class.java, fragments)

        repository.throwingCheckedException()
    }


    private interface SimpleSampleRepository : RequeryRepository<User, Int> {

        @Transactional
        override fun findById(id: Int): Optional<User>
    }

    interface SampleCustomRepository {

        fun throwingRuntimeException()

        @Throws(IOException::class)
        fun throwingCheckedException()
    }

    class SampleCustomRepositoryImpl : SampleCustomRepository {

        override fun throwingRuntimeException() {
            throw IllegalArgumentException("You lose!")
        }

        @Throws(IOException::class)
        override fun throwingCheckedException() {
            throw IOException("You lose!")
        }
    }

    private interface SampleRepository : RequeryRepository<User, Int>, SampleCustomRepository {

        fun findByEmail(email: String): User? = UserEntity().also { it.emailAddress = email }

        fun customMethod(id: Long?): User? = UserEntity()
    }

    class CustomRequeryRepository<T : Any, ID : Any>(
        entityInformation: RequeryEntityInformation<T, ID>,
        operations: RequeryOperations)
        : SimpleRequeryRepository<T, ID>(entityInformation, operations)
}