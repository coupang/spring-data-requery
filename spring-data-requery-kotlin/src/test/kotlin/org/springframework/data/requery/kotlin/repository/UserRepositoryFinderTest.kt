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

package org.springframework.data.requery.kotlin.repository

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.domain.Sort.by
import org.springframework.data.requery.kotlin.configs.RequeryTestConfiguration
import org.springframework.data.requery.kotlin.domain.sample.RoleEntity
import org.springframework.data.requery.kotlin.domain.sample.UserEntity
import org.springframework.data.requery.kotlin.repository.config.EnableRequeryRepositories
import org.springframework.data.requery.kotlin.repository.sample.RoleRepository
import org.springframework.data.requery.kotlin.repository.sample.UserRepository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * org.springframework.data.requery.kotlin.repository.UserRepositoryFinderTest
 *
 * @author debop
 */
@RunWith(SpringRunner::class)
@ContextConfiguration
@Transactional
class UserRepositoryFinderTest {

    companion object {
        private val log = KotlinLogging.logger { }

        fun createUser(): UserEntity {
            return UserEntity().also {
                it.active = true
                it.createdAt = Timestamp.valueOf(LocalDateTime.now())
            }
        }

        fun createUser(firstname: String, lastname: String, email: String, vararg roles: RoleEntity): UserEntity {
            return createUser().also {
                it.firstname = firstname
                it.lastname = lastname
                it.emailAddress = email

                if(roles.isNotEmpty()) {
                    it.roles.addAll(roles)
                }
            }
        }
    }

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = [UserRepository::class])
    class TestConfiguration : RequeryTestConfiguration()

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    lateinit var dave: UserEntity
    lateinit var carter: UserEntity
    lateinit var oliver: UserEntity

    lateinit var drummer: RoleEntity
    lateinit var guitarist: RoleEntity
    lateinit var singer: RoleEntity

    @Before
    fun setup() {
        roleRepository.deleteAll()
        userRepository.deleteAll()

        drummer = roleRepository.save(RoleEntity().apply { name = "DRUMMER" })
        guitarist = roleRepository.save(RoleEntity().apply { name = "GUITARIST" })
        singer = roleRepository.save(RoleEntity().apply { name = "SINGER" })

        dave = userRepository.save(createUser("Dave", "Matthews", "dave@dmband.com", singer))
        carter = userRepository.save(createUser("Carter", "Beauford", "carter@dmband.com", singer, drummer))
        oliver = userRepository.save(createUser("Oliver Auguest", "Matthews", "oliver@dmband.com"))
    }

    @After
    fun cleanup() {
        userRepository.deleteAll()
        roleRepository.deleteAll()
    }

    @Test
    fun `simple custom created finder`() {

        val user = userRepository.findByEmailAddressAndLastname("dave@dmband.com", "Matthews")
        assertThat(user).isEqualTo(dave)
    }

    @Test
    fun `returns null if nothing found`() {

        val user = userRepository.findByEmailAddress("foobar")
        assertThat(user).isNull()
    }

    @Test
    fun `where with and or finder`() {

        val users = userRepository.findByEmailAddressAndLastnameOrFirstname("dave@dmband.com", "Matthews", "Carter")

        assertThat(users)
            .hasSize(2)
            .containsOnly(dave, carter)
    }

    @Test
    fun `executes paging method to page correctly`() {

        val users = userRepository.findByFirstname("Carter", PageRequest.of(0, 1))
        assertThat(users).hasSize(1)
    }

    @Test
    fun `execute In keyword for page`() {

        val page = userRepository.findByFirstnameIn(PageRequest.of(0, 1), "Dave", "Oliver Auguest")

        assertThat(page.numberOfElements).isEqualTo(1)
        assertThat(page.totalElements).isEqualTo(2L)
        assertThat(page.totalPages).isEqualTo(2)
    }

    @Test
    fun `execute NotIn query`() {
        val result = userRepository.findByFirstnameNotIn(arrayListOf("Dave", "Carter"))
        assertThat(result).hasSize(1).containsOnly(oliver)
    }

    @Test
    fun `find by lastname ignoring case`() {

        val result = userRepository.findByLastnameIgnoringCase("BeAUfoRd")
        assertThat(result).hasSize(1).containsOnly(carter)
    }

    @Test
    fun `find by lastname ignoring case like`() {

        val result = userRepository.findByLastnameIgnoringCaseLike("BeAUfo%")
        assertThat(result).hasSize(1).containsOnly(carter)
    }

    @Test
    fun `find by lastname and firstname all ignoring case`() {

        val result = userRepository.findByLastnameAndFirstnameAllIgnoringCase("MaTTheWs", "DaVe")
        assertThat(result).hasSize(1).containsOnly(dave)
    }

    @Test
    fun `respects pageable order on query generate from method name`() {

        val ascending = userRepository.findByLastnameIgnoringCase(PageRequest.of(0, 10, by(ASC, "firstname")),
                                                                  "Matthews")

        val descending = userRepository.findByLastnameIgnoringCase(PageRequest.of(0, 10, by(DESC, "firstname")),
                                                                   "Matthews")

        assertThat(ascending.totalElements).isEqualTo(2L)
        assertThat(descending.totalElements).isEqualTo(2L)

        assertThat(ascending.content[0].firstname).isNotEqualTo(descending.content[0].firstname)
        assertThat(ascending.content[1].firstname).isEqualTo(descending.content[0].firstname)
    }

    @Test
    fun `executes query to slice`() {

        val slice = userRepository.findSliceByLastname("Matthews",
                                                       PageRequest.of(0, 1, ASC, "firstname"))

        assertThat(slice.content).contains(dave)
        assertThat(slice.hasNext()).isTrue()
    }

    @Test
    fun `executes method with not containing on string correctly`() {
        val users = userRepository.findByLastnameNotContaining("u")
        assertThat(users).containsOnly(dave, oliver)
    }

    @Test
    fun `translates contains to member of`() {

        val singers = userRepository.findByRolesContaining(singer)

        assertThat(singers).hasSize(2).contains(dave, carter)

        assertThat(userRepository.findByRolesContaining(drummer)).contains(carter)
    }

    @Test
    fun `translates not contains to member of`() {
        assertThat(userRepository.findByRolesNotContaining(drummer)).contains(dave, oliver)
    }

    @Ignore("Transanction propagation 이 NOT_SUPPORTED 일때 예외를 발생해야 한다.")
    @Test // (expected = InvalidDataAccessApiUsageException.class) // DATAJPA-1023, DATACMNS-959
    fun rejectsStreamExecutionIfNoSurroundingTransactionActive() {
        userRepository.findAllByCustomQueryAndStream()
    }

    @Ignore("Not support Named query")
    @Test // DATAJPA-1334
    fun executesNamedQueryWithConstructorExpression() {
        userRepository.findByNamedQueryWithConstructorExpression()
    }
}