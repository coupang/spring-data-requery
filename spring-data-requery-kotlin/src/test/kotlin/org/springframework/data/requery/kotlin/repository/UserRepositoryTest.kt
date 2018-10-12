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

import io.requery.sql.StatementExecutionException
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.ExampleMatcher.matching
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.requery.kotlin.configs.RequeryTestConfiguration
import org.springframework.data.requery.kotlin.core.RequeryOperations
import org.springframework.data.requery.kotlin.domain.sample.RoleEntity
import org.springframework.data.requery.kotlin.domain.sample.UserEntity
import org.springframework.data.requery.kotlin.repository.config.EnableRequeryRepositories
import org.springframework.data.requery.kotlin.repository.sample.UserRepository
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertTrue

/**
 * UserRepositoryTest
 *
 * @author debop
 */
@RunWith(SpringRunner::class)
@ContextConfiguration
class UserRepositoryTest {

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

        fun <E> assertSameElements(first: Collection<E>, second: Collection<E>) {

            assertThat(first.size).isEqualTo(second.size)
            assertThat(first.all { second.contains(it) }).isTrue()
            assertThat(second.all { first.contains(it) }).isTrue()
        }
    }

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = [UserRepository::class])
    class TestConfiguration : RequeryTestConfiguration() {

        //        @Autowired lateinit var applicationContext: ApplicationContext
        //        @Autowired lateinit var operations: RequeryOperations
        //
        //        @Bean
        //        fun usrRepository(): UserRepository {
        //
        //        }
        //
        //        @Bean
        //        fun roleRepository(): RoleRepository {
        //
        //        }

    }

    @Autowired
    lateinit var operations: RequeryOperations

    @Autowired
    lateinit var repository: UserRepository

    lateinit var firstUser: UserEntity
    lateinit var secondUser: UserEntity
    lateinit var thirdUser: UserEntity
    lateinit var fourthUser: UserEntity

    lateinit var adminRole: RoleEntity

    var id: Int? = null


    @Before
    @Throws(Exception::class)
    fun setup() {

        firstUser = createUser("Debop", "Bae", "debop@example.com")
        firstUser.age = 51

        secondUser = createUser("Diego", "Ahn", "diego@example.com")
        secondUser.age = 30

        Thread.sleep(10)

        thirdUser = createUser("Jinie", "Park", "jinie@example.com")
        thirdUser.age = 26

        fourthUser = createUser("Nickoon", "Jeon", "nickoon@example.com")
        fourthUser.age = 35

        adminRole = RoleEntity().apply { name = "admin" }

        repository.deleteAll()
    }

    @Test
    fun testCreation() {

        val before = operations.count(UserEntity::class).get().value()

        flushTestUsers()

        assertThat(operations.count(UserEntity::class).get().value()).isEqualTo(before!! + 4)
    }

    @Test
    fun testRead() {

        flushTestUsers()

        assertThat(repository.findById(id!!).map { it.firstname }).contains(firstUser.firstname)
    }

    @Test
    fun findAllByGivenIds() {
        flushTestUsers()
        assertThat(repository.findAllById(arrayListOf(firstUser.id!!, secondUser.id!!))).contains(firstUser, secondUser)
    }

    @Test
    fun testReadByIdReturnsNullForNotFoundEntities() {

        flushTestUsers()
        assertThat(repository.findById(-27 * id!!)).isNotPresent
    }

    @Test
    fun savesCollectionCorrectly() {
        val savedUsers = repository.saveAll(Arrays.asList(firstUser, secondUser, thirdUser))

        assertThat(savedUsers).hasSize(3).containsOnly(firstUser, secondUser, thirdUser)
        savedUsers.forEach { assertThat(it.id!!).isNotNull() }
    }

    @Test
    fun savingEmptyCollectionIsNoOp() {

        assertThat(repository.saveAll(arrayListOf<UserEntity>())).isEmpty()
    }

    @Test
    fun testUpdate() {

        flushTestUsers()

        val foundPerson = repository.findById(id!!).orElseThrow { IllegalStateException("Not Found") }
        foundPerson.lastname = "Kwon"

        repository.upsert(foundPerson)
        repository.refresh(foundPerson)

        assertThat(repository.findById(id!!).map { it.firstname }).contains(foundPerson.firstname)
    }

    @Test
    fun existReturnsWhetherAnEntityCanBeLoaded() {
        flushTestUsers()

        assertThat(repository.existsById(id!!)).isTrue()
        assertThat(repository.existsById(-27 * id!!)).isFalse()
    }

    @Test
    fun deletesAUserById() {
        flushTestUsers()

        repository.deleteById(firstUser.id!!)
    }

    @Test
    fun testDelete() {
        flushTestUsers()

        repository.delete(firstUser)

        assertThat(repository.existsById(id!!)).isFalse()
        assertThat(repository.findById(id!!)).isNotPresent
    }

    @Test
    fun returnsAllSortedCorrectly() {
        flushTestUsers()

        assertThat(repository.findAll(Sort.by(Sort.Direction.ASC, "lastname")))
            .hasSize(4)
            .containsExactly(secondUser, firstUser, fourthUser, thirdUser)
    }

    @Test
    fun deleteCollectionOfEntities() {
        flushTestUsers()

        val before = repository.count()

        repository.deleteAll(Arrays.asList(firstUser, secondUser))

        assertThat(repository.existsById(firstUser.id!!)).isFalse()
        assertThat(repository.existsById(secondUser.id!!)).isFalse()

        assertThat(repository.count()).isEqualTo(before - 2)
    }

    @Test
    fun batchDeleteCollectionOfEntities() {
        flushTestUsers()

        val before = repository.count()

        repository.deleteInBatch(Arrays.asList(firstUser, secondUser))

        assertThat(repository.existsById(firstUser.id!!)).isFalse()
        assertThat(repository.existsById(secondUser.id!!)).isFalse()

        assertThat(repository.count()).isEqualTo(before - 2)
    }

    @Test
    fun deleteEmptyCollectionDoesNotDeleteAnything() {
        assertDeleteCallDoesNotDeleteAnything(arrayListOf<UserEntity>())
    }

    @Test
    fun executesManipulatingQuery() {
        flushTestUsers()

        repository.renameAllUsersTo("newLastname")

        val expected = repository.count()
        assertThat(repository.findByLastname("newLastname")).hasSize(expected.toInt())
    }

    @Test
    fun testFinderInvocationWithNullParameter() {

        flushTestUsers()

        repository.findByLastname(null)
    }

    @Test
    fun testFindByLastname() {
        flushTestUsers()
        assertThat(repository.findByLastname("Bae")).containsOnly(firstUser)
    }

    @Test
    fun testFindByEmailAddress() {
        flushTestUsers()
        assertThat(repository.findByEmailAddress("debop@example.com")).isEqualTo(firstUser)
    }

    @Test
    fun testReadAll() {
        flushTestUsers()

        assertThat(repository.count()).isEqualTo(4L)
        assertThat(repository.findAll()).containsOnly(firstUser, secondUser, thirdUser, fourthUser)
    }

    @Test
    fun deleteAll() {
        flushTestUsers()

        repository.deleteAll()
        assertThat(repository.count()).isZero()
    }

    @Test
    fun deleteAllInBatch() {
        flushTestUsers()

        repository.deleteAllInBatch()
        assertThat(repository.count()).isZero()
    }

    @Test
    fun testCascadesPersisting() {

        // Create link prior to persisting
        firstUser.colleagues.add(secondUser)

        flushTestUsers()

        val firstReferenceUser = repository.findById(firstUser.id!!).get()
        assertThat(firstReferenceUser).isEqualTo(firstUser)

        val colleagues = firstReferenceUser.colleagues
        assertThat(colleagues).containsOnly(secondUser)
    }

    @Test
    fun testPreventsCascadingRolePersisting() {

        firstUser.roles.add(RoleEntity().apply { name = "USER" })
        flushTestUsers()
    }

    @Test
    fun testUpsertCascadesCollegues() {

        firstUser.colleagues.add(secondUser)
        flushTestUsers()

        firstUser.colleagues.add(createUser("Tao", "Kim", "tagkim@example.com"))
        firstUser = repository.upsert(firstUser)

        val reference = repository.findById(firstUser.id!!).get()
        val colleagues = reference.colleagues

        assertThat(colleagues).hasSize(2).contains(secondUser)
    }

    @Test
    fun testCountsCorrectly() {

        val count = repository.count()

        val user = createUser("Jane", "Doe", "janedoe@example.com")
        repository.save(user)

        assertThat(repository.count()).isEqualTo(count + 1)
    }

    @Test
    fun testInvocationOfCustomImplementation() {

        repository.someCustomMethod(UserEntity())
    }

    @Test
    fun testOverwritingFinder() {

        repository.findByOverrridingMethod()
    }

    @Test
    fun testUsesQueryAnnotation() {
        assertThat(repository.findByAnnotatedQuery("debop@example.com")).isNull()

        flushTestUsers()
        assertThat(repository.findByAnnotatedQuery("debop@example.com")).isEqualTo(firstUser)
    }

    @Test
    fun testExecutionOfProjectingMethod() {
        flushTestUsers()

        assertThat(repository.countWithFirstname("Debop")).isEqualTo(1L)
    }

    @Test
    fun executesSpecificationCorrectly() {
        flushTestUsers()
        val query = repository.operations
            .select(UserEntity::class)
            .where(UserEntity.FIRSTNAME.eq("Debop"))
            .unwrap()

        assertThat(repository.findAll(query)).hasSize(1).containsOnly(firstUser)
    }

    @Test
    fun executesSingleEntitySpecificationCorrectly() {
        flushTestUsers()
        val query = repository.operations
            .select(UserEntity::class).where(UserEntity.FIRSTNAME.eq("Debop"))
            .unwrap()

        assertThat(repository.findOne(query)).isPresent()
    }

    @Test(expected = IncorrectResultSizeDataAccessException::class)
    fun throwsExceptionForUnderSpecifiedSingleEntitySpecification() {
        flushTestUsers()
        val query = repository.operations
            .select(UserEntity::class).where(UserEntity.FIRSTNAME.like("%e%"))
            .unwrap()

        // 2개 이상이 나온다
        repository.findOne(query)
    }

    @Test
    fun executesCombinedSpecificationsCorrectly() {
        flushTestUsers()

        val query = repository.operations
            .select(UserEntity::class)
            .where(UserEntity.FIRSTNAME.eq("Debop"))
            .or(UserEntity.LASTNAME.eq("Ahn"))
            .unwrap()

        assertThat(repository.findAll(query)).hasSize(2).containsOnly(firstUser, secondUser)

    }

    @Test
    fun executesNegatingSpecificationCorrectly() {
        flushTestUsers()

        val query = repository.operations
            .select(UserEntity::class)
            .where(UserEntity.FIRSTNAME.ne("Debop"))
            .and(UserEntity.LASTNAME.eq("Ahn"))
            .unwrap()

        assertThat(repository.findAll(query)).hasSize(1).containsOnly(secondUser)
    }

    @Test
    fun executesCombinedSpecificationsWithPageableCorrectly() {
        flushTestUsers()

        val query = repository.operations
            .select(UserEntity::class)
            .where(UserEntity.FIRSTNAME.eq("Debop"))
            .or(UserEntity.LASTNAME.eq("Ahn"))
            .unwrap()

        val users = repository.findAll(query, PageRequest.of(0, 1))
        assertThat(users.size).isEqualTo(1)
        assertThat(users.hasPrevious()).isFalse()
        assertThat(users.totalElements).isEqualTo(2L)
    }

    @Test
    fun executesMethodWithAnnotatedNamedParametersCorrectly() {

        firstUser = repository.save(firstUser)
        secondUser = repository.save(secondUser)

        assertThat(repository.findByLastnameOrFirstname("Ahn", "Debop"))
            .hasSize(2)
            .containsOnly(firstUser, secondUser)
    }

    @Test
    fun executesMethodWithNamedParametersCorrectlyOnMethodsWithQueryCreation() {

        firstUser = repository.save(firstUser)
        secondUser = repository.save(secondUser)

        assertThat(repository.findByFirstnameOrLastname("Debop", "Ahn"))
            .hasSize(2)
            .containsOnly(firstUser, secondUser)
    }

    @Test
    fun executesLikeAndOrderByCorrectly() {
        flushTestUsers()

        assertThat(repository.findByLastnameLikeOrderByFirstnameDesc("%a%"))
            .hasSize(2)
            .containsExactly(thirdUser, firstUser)
    }

    @Test
    fun executesNotLikeCorrectly() {
        flushTestUsers()

        assertThat(repository.findByLastnameNotLike("%ae%"))
            .hasSize(3)
            .containsOnly(secondUser, thirdUser, fourthUser)
    }

    @Test
    fun executesSimpleNotCorrectly() {
        flushTestUsers()

        assertThat(repository.findByLastnameNot("Bae"))
            .hasSize(3)
            .containsOnly(secondUser, thirdUser, fourthUser)
    }

    @Test
    fun returnsSameListIfNoSpecGiven() {

        flushTestUsers()
        assertSameElements(repository.findAll(), repository.findAll(operations.select(UserEntity::class)))
    }

    @Test
    fun returnsSameListIfNoSortIsGiven() {

        flushTestUsers()
        assertSameElements(repository.findAll(Sort.unsorted()), repository.findAll())
    }

    @Test
    fun returnsAllAsPageIfNoPageableIsGiven() {

        flushTestUsers()
        assertThat(repository.findAll(Pageable.unpaged())).isEqualTo(PageImpl(repository.findAll()))
    }

    @Test
    fun removeObject() {

        flushTestUsers()
        val count = repository.count()

        repository.delete(firstUser)
        assertThat(repository.count()).isEqualTo(count - 1)
    }

    @Test
    fun executesPagedSpecificationsCorrectly() {

        val result = executeSpecWithSort(Sort.unsorted())
        assertThat(result.content).isSubsetOf(firstUser, thirdUser)
    }

    @Test
    fun executesPagedSpecificationsWithSortCorrectly() {

        val result = executeSpecWithSort(Sort.by(Sort.Direction.ASC, "lastname"))
        assertThat(result.content).contains(firstUser).doesNotContain(secondUser, thirdUser)
    }

    // NOTE: Not Supported
    @Test(expected = Exception::class)
    fun executesQueryMethodWithDeepTraversalCorrectly() {

        flushTestUsers()

        firstUser.manager = secondUser
        thirdUser.manager = firstUser
        repository.saveAll(Arrays.asList(firstUser, thirdUser))

        assertThat(repository.findByManagerLastname("Ahn")).containsOnly(firstUser)
        assertThat(repository.findByManagerLastname("Bae")).containsOnly(thirdUser)
    }

    // NOTE: Not Supported
    @Test(expected = Exception::class)
    fun executesFindByColleaguesLastnameCorrectly() {

        flushTestUsers()

        firstUser.colleagues.add(secondUser)
        thirdUser.colleagues.add(firstUser)
        repository.saveAll(Arrays.asList(firstUser, thirdUser))

        assertThat(repository.findByColleaguesLastname(secondUser.lastname)).containsOnly(firstUser)
        assertThat(repository.findByColleaguesLastname("Bae")).containsOnly(secondUser, thirdUser)
    }

    @Test
    fun executesFindByNotNullLastnameCorrectly() {

        flushTestUsers()

        assertThat(repository.findByLastnameNotNull()).containsOnly(firstUser, secondUser, thirdUser, fourthUser)
    }

    @Test
    fun findsSortedByLastname() {

        flushTestUsers()

        assertThat(repository.findByEmailAddressLike("%@%", Sort.by(Sort.Direction.ASC, "lastname")))
            .containsExactly(secondUser, firstUser, fourthUser, thirdUser)
    }

    @Test
    fun readsPageWithGroupByClauseCorrectly() {

        flushTestUsers()

        val result = repository.findByLastnameGrouped(PageRequest.of(0, 10))
        assertThat(result.totalPages).isEqualTo(1)
    }

    @Test
    fun executesLessThatOrEqualQueriesCorrectly() {

        flushTestUsers()

        assertThat(repository.findByAgeLessThanEqual(35)).containsOnly(secondUser, thirdUser, fourthUser)
    }

    @Test
    fun executesGreaterThatOrEqualQueriesCorrectly() {

        flushTestUsers()

        assertThat(repository.findByAgeGreaterThanEqual(35)).containsOnly(firstUser, fourthUser)
    }

    @Test
    fun executesNativeQueryCorrectly() {

        flushTestUsers()
        assertThat(repository.findNativeByLastname("Bae")).containsOnly(firstUser)
    }

    @Test
    fun executesFinderWithTrueKeywordCorrectly() {

        flushTestUsers()
        firstUser.active = false
        repository.upsert(firstUser)

        assertThat(repository.findByActiveTrue()).containsOnly(secondUser, thirdUser, fourthUser)
    }

    @Test
    fun executesFinderWithFalseKeywordCorrectly() {

        flushTestUsers()
        firstUser.active = false
        repository.upsert(firstUser)

        assertThat(repository.findByActiveFalse()).containsOnly(firstUser)
    }

    @Test
    @Throws(InterruptedException::class)
    fun executesAnnotatedCollectionMethodCorrectly() {

        flushTestUsers()

        firstUser.colleagues.add(thirdUser)
        repository.save(firstUser)

        val result = repository.findColleaguesFor(firstUser.id)
        assertThat(result).containsOnly(thirdUser)
    }

    @Test
    fun executesFinderWithAfterKeywordCorrectly() {

        flushTestUsers()
        assertThat(repository.findByCreatedAtAfter(secondUser.createdAt!!)).containsOnly(thirdUser, fourthUser)
    }

    @Test
    fun executesFinderWithBeforeKeywordCorrectly() {

        flushTestUsers()
        assertThat(repository.findByCreatedAtBefore(thirdUser.createdAt!!)).containsOnly(firstUser, secondUser)
    }

    @Test
    fun executesFinderWithStartingWithCorrectly() {

        flushTestUsers()
        assertThat(repository.findByFirstnameStartingWith("Deb")).containsOnly(firstUser)
    }

    @Test
    fun executesFinderWithEndingWithCorrectly() {

        flushTestUsers()
        assertThat(repository.findByFirstnameEndingWith("bop")).containsOnly(firstUser)
    }

    @Test
    fun executesFinderWithContainingCorrectly() {

        flushTestUsers()
        assertThat(repository.findByFirstnameContaining("n")).containsOnly(thirdUser, fourthUser)
    }

    @Test
    fun allowsExecutingPageableMethodWithUnpagedArgument() {

        flushTestUsers()

        assertThat(repository.findByFirstname("Debop", Pageable.unpaged())).containsOnly(firstUser)

        var page = repository.findByFirstnameIn(Pageable.unpaged(), "Debop", "Diego")
        assertThat(page).isNotNull
        assertThat(page.numberOfElements).isEqualTo(2)
        assertThat(page.content).containsOnly(firstUser, secondUser)

        page = repository.findAll(Pageable.unpaged())
        assertThat(page.numberOfElements).isEqualTo(4)
        assertThat(page.content).contains(firstUser, secondUser, thirdUser, fourthUser)
    }

    @Test
    fun executesNativeQueryForNonEntitiesCorrectly() {

        flushTestUsers()

        val result = repository.findOnesByNativeQuery()

        assertThat(result.size).isEqualTo(4)
        assertThat(result[0].get<Int>(0)).isEqualTo(1)
    }

    @Test
    fun handlesIterableOfIdsCorrectly() {

        flushTestUsers()

        val set = HashSet<Int>()
        set.add(firstUser.id!!)
        set.add(secondUser.id!!)

        assertThat(repository.findAllById(set)).containsOnly(firstUser, secondUser)
    }


    protected fun flushTestUsers() {

        operations.upsert(adminRole)

        firstUser = repository.save(firstUser)
        secondUser = repository.save(secondUser)
        thirdUser = repository.save(thirdUser)
        fourthUser = repository.save(fourthUser)

        id = firstUser.id

        assertThat(id).isNotNull()
        assertThat(secondUser.id).isNotNull()
        assertThat(thirdUser.id).isNotNull()
        assertThat(fourthUser.id).isNotNull()

        assertThat(repository.existsById(id!!)).isTrue()
        assertThat(repository.existsById(secondUser.id!!)).isTrue()
        assertThat(repository.existsById(thirdUser.id!!)).isTrue()
        assertThat(repository.existsById(fourthUser.id!!)).isTrue()
    }

    private fun assertDeleteCallDoesNotDeleteAnything(collection: List<UserEntity>) {

        flushTestUsers()
        val count = repository.count()

        repository.deleteAll(collection)
        assertThat(repository.count()).isEqualTo(count)
    }

    @Test
    fun ordersByReferencedEntityCorrectly() {

        flushTestUsers()
        firstUser.manager = thirdUser
        repository.upsert(firstUser)

        val all = repository.findAll(PageRequest.of(0, 10, Sort.by("manager")))

        assertThat(all.content).isNotEmpty
    }

    @Test
    fun bindsSortingToOuterJoinCorrectly() {

        flushTestUsers()

        val result = repository.findAllPaged(PageRequest.of(0, 10, Sort.by("manager.lastname")))
        assertThat(result.content).hasSize(repository.count().toInt())
    }

    @Test
    fun doesNotDropNullValuesOnPagedSpecificationExecution() {

        flushTestUsers()

        // Not support association property. (ex, manager.lastname)
        val filter = operations
            .select(UserEntity::class)
            .where(UserEntity.LASTNAME.eq("Bae"))
            .unwrap()
        val page = repository.findAll(filter, PageRequest.of(0, 20, Sort.by("manager.lastname")))

        assertThat(page.numberOfElements).isEqualTo(1)
        assertThat(page).containsOnly(firstUser)
    }

    @Test
    fun shouldGenerateLeftOuterJoinInfindAllWithPaginationAndSortOnNestedPropertyPath() {

        firstUser.manager = null
        secondUser.manager = null
        thirdUser.manager = firstUser    // manager Debop
        fourthUser.manager = secondUser  // manager Diego

        flushTestUsers()

        // Not support association property. (ex, manager.lastname)
        val pages = repository.findAll(PageRequest.of(0, 4, Sort.by("manager")))

        assertThat(pages.size).isEqualTo(4)
        assertThat(pages.content[0].manager).isNull()
        assertThat(pages.content[1].manager).isNull()
        assertThat(pages.content[2].manager?.firstname).isEqualTo("Debop")
        assertThat(pages.content[3].manager?.firstname).isEqualTo("Diego")
    }

    @Test
    fun executesManualQueryWithPositionLikeExpressionCorrectly() {

        flushTestUsers()

        val result = repository.findByFirstnameLike("Ni%")
        assertThat(result).containsOnly(fourthUser)
    }

    // Not supported Named parameter currently
    @Test(expected = StatementExecutionException::class)
    fun executesManualQueryWithNamedLikeExpressionCorrectly() {

        flushTestUsers()

        val result = repository.findByFirstnameLikeNamed("Ni%")
        assertThat(result).containsOnly(fourthUser)
    }

    @Test
    fun executesDerivedCountQueryToLong() {

        flushTestUsers()
        assertThat(repository.countByLastname("Bae")).isEqualTo(1L)
    }

    @Test
    fun executesDerivedCountQueryToInt() {

        flushTestUsers()

        assertThat(repository.countUsersByFirstname("Debop")).isEqualTo(1)
    }

    @Test
    fun executesDerivedExistsQuery() {

        flushTestUsers()

        assertThat(repository.existsByLastname("Bae")).isTrue()
        assertThat(repository.existsByLastname("Donald Trump")).isFalse()
    }

    @Test
    fun findAllReturnsEmptyIterableIfNoIdsGiven() {

        assertThat(repository.findAllById(emptySet())).isEmpty()
    }

    @Ignore("결과가 Tuple 인 경우 ReturnedType 으로 변환하는 기능이 필요하다")
    @Test
    fun executesManuallyDefinedQueryWithFieldProjection() {

        flushTestUsers()
        val firstnames = repository.findFirstnamesByLastname("Bae")

        firstnames.forEach { firstname -> log.debug("firstname={}", firstname) }
        assertThat(firstnames).containsOnly("Debop")
    }

    @Test
    fun looksUpEntityReference() {

        flushTestUsers()

        val result = repository.getOne(firstUser.id!!)
        assertThat(result).isEqualTo(firstUser)
    }

    @Test
    fun invokesQueryWithVarargsParametersCorrectly() {

        flushTestUsers()

        val result = repository.findByIdIn(firstUser.id!!, secondUser.id!!)
        assertThat(result).containsOnly(firstUser, secondUser)
    }

    @Test
    fun shouldSupportModifyingQueryWithVarArgs() {

        flushTestUsers()

        repository.updateUserActiveState(false, firstUser.id!!, secondUser.id!!, thirdUser.id!!, fourthUser.id!!)

        val expectedCount = repository.count()
        assertThat(repository.findByActiveFalse()).hasSize(expectedCount.toInt())
        assertThat(repository.findByActiveTrue()).isEmpty()
    }

    @Test
    fun executesFinderWithOrderClauseOnly() {

        flushTestUsers()

        assertThat(repository.findAllByOrderByLastnameAsc())
            .containsExactly(secondUser, firstUser, fourthUser, thirdUser)
    }


    // NOTE: Not Supported
    @Test
    fun sortByEmbeddedProperty() {

        thirdUser.address!!.country = "South Korea"
        thirdUser.address!!.city = "Seoul"
        thirdUser.address!!.streetName = "Songpa"
        thirdUser.address!!.streetNo = "570"

        flushTestUsers()

        val page = repository.findAll(PageRequest.of(0, 10, Sort.by("address.streetName")))
        assertThat(page.content).hasSize(4)

        // Not supported
        // assertThat(page.getContent().get(3)).isEqualTo(thirdUser);
    }

    @Test
    fun findsUserByBinaryDataReference() {

        flushTestUsers()

        val result = repository.findByIdsCustomWithPositionalVarArgs(firstUser.id!!, secondUser.id!!)

        assertThat(result).containsOnly(firstUser, secondUser)
    }

    @Test
    fun customFindByQueryWithNamedVarargsParameters() {

        flushTestUsers()

        val result = repository.findByIdsCustomWithNamedVarArgs(firstUser.id!!, secondUser.id!!)

        assertThat(result).containsOnly(firstUser, secondUser)
    }

    @Ignore("Not support derived class from entity class")
    @Test
    fun saveAndFlushShouldSupportReturningSubTypesOfRepositoryEntity() {

        //        val user = SpecialUser()
        //        user.setFirstname("Thomas")
        //        user.setEmailAddress("thomas@example.org")
        //
        //        // HINT: Entity class 를 상속받은 Derived class를 부모 클래스용 Repository를 사용하면 안된다.
        //        val savedUser = repository.insert(user)
        //
        //        assertThat(savedUser.firstname).isEqualTo(user.firstname)
        //        assertThat(savedUser.emailAddress).isEqualTo(user.getEmailAddress())
    }

    @Test
    fun findAllByUntypedExampleShouldReturnSubTypesOfRepositoryEntity() {

        flushTestUsers()

        val result = repository.findAll(Example.of(createUser(),
                                                   matching().withIgnorePaths("age", "createdAt", "dateOfBirth")))
        assertThat(result).hasSize(4)
    }

    @Test
    fun findAllByTypedUserExampleShouldReturnSubTypesOfRepositoryEntity() {

        flushTestUsers()

        val example = Example.of(createUser(),
                                 matching().withIgnorePaths("age", "createdAt", "dateOfBirth"))
        val result = repository.findAll(example)

        assertThat(result).hasSize(4)
    }

    @Test
    fun deleteByShouldReturnListOfDeletedElementsWhenRetunTypeIsCollectionLike() {

        flushTestUsers()

        val deletedCount = repository.deleteByLastname(firstUser.lastname)
        assertThat(deletedCount).isEqualTo(1)

        assertThat(repository.countByLastname(firstUser.lastname)).isEqualTo(0L)
    }

    @Test
    fun deleteByShouldReturnNumberOfEntitiesRemovedIfReturnTypeIsInteger() {

        flushTestUsers()

        val removedCount = repository.removeByLastname(firstUser.lastname)
        assertThat(removedCount).isEqualTo(1)
    }


    @Test
    fun deleteByShouldReturnZeroInCaseNoEntityHasBeenRemovedAndReturnTypeIsNumber() {

        flushTestUsers()

        val removedCount = repository.removeByLastname("Not exists")
        assertThat(removedCount).isEqualTo(0)
    }

    @Ignore("Tuple 을 returned type 으로 추출하는 작업이 필요하다.")
    @Test
    fun findBinaryDataByIdNative() {

        val data = "Woho!!".toByteArray(StandardCharsets.UTF_8)
        firstUser.binaryData = data

        flushTestUsers()

        // TODO: Tuple 을 returned type 으로 추출하는 작업이 필요하다.
        // TODO: @Convert 를 사용한 property 에 대해서 변환작업도 필요하다. Blob 를 byte[] 로 바꾸는 ...
        val result = repository.findBinaryDataByIdNative(firstUser.id)

        assertThat(result).isNotNull()
        assertThat(result.size).isEqualTo(data.size)
        assertThat(result).isEqualTo(data)
    }

    @Test
    fun findPaginatedExplicitQueryWithEmpty() {

        firstUser.firstname = null

        flushTestUsers()

        val result = repository.findAllByFirstnameLike("%", PageRequest.of(0, 10))
        assertThat(result.content).hasSize(3)
    }

    @Test
    fun findPaginatedExplicitQuery() {

        flushTestUsers()

        val result = repository.findAllByFirstnameLike("De%", PageRequest.of(0, 10))
        assertThat(result.content).hasSize(1)
    }

    @Test
    fun findOldestUser() {

        flushTestUsers()

        val oldest = firstUser

        assertThat(repository.findFirstByOrderByAgeDesc()).isEqualTo(oldest)
        assertThat(repository.findFirst1ByOrderByAgeDesc()).isEqualTo(oldest)
    }

    @Test
    fun findYoungestUser() {

        flushTestUsers()

        val youngest = thirdUser

        assertThat(repository.findTopByOrderByAgeAsc()).isEqualTo(youngest)
        assertThat(repository.findTop1ByOrderByAgeAsc()).isEqualTo(youngest)
    }

    @Test
    fun find2OldestUser() {

        flushTestUsers()

        val oldest1 = firstUser
        val oldest2 = fourthUser

        assertThat(repository.findFirst2ByOrderByAgeDesc()).containsOnly(oldest1, oldest2)
        assertThat(repository.findTop2ByOrderByAgeDesc()).containsOnly(oldest1, oldest2)
    }

    @Test
    fun find2YoungestUser() {

        flushTestUsers()

        val youngest1 = thirdUser
        val youngest2 = secondUser

        assertThat(repository.findFirst2UsersBy(Sort.by("age"))).containsOnly(youngest1, youngest2)
        assertThat(repository.findTop2UsersBy(Sort.by("age"))).containsOnly(youngest1, youngest2)
    }

    @Test
    fun find3YoungestUsersPageableWithPageSize2() {

        flushTestUsers()

        val youngest1 = secondUser
        val youngest2 = thirdUser
        val youngest3 = fourthUser

        val firstPage = repository.findFirst3UsersBy(PageRequest.of(0, 2, Sort.by("age")))
        assertThat(firstPage.content).contains(youngest1, youngest2)

        val secondPage = repository.findFirst3UsersBy(PageRequest.of(1, 2, Sort.by("age")))
        assertThat(secondPage.content).contains(youngest3)
    }

    @Test
    fun find2YoungestUsersPageableWithPageSize3() {

        flushTestUsers()

        val youngest1 = secondUser
        val youngest2 = thirdUser
        val youngest3 = fourthUser

        val firstPage = repository.findFirst2UsersBy(PageRequest.of(0, 3, Sort.by("age")))
        assertThat(firstPage.content).contains(youngest1, youngest2)

        val secondPage = repository.findFirst2UsersBy(PageRequest.of(1, 3, Sort.by("age")))
        assertThat(secondPage.content).contains(youngest3)
    }

    @Test
    fun find3YoungestUsersPageableWithPageSize2Sliced() {

        flushTestUsers()

        val youngest1 = secondUser
        val youngest2 = thirdUser
        val youngest3 = fourthUser

        val firstPage = repository.findTop3UsersBy(PageRequest.of(0, 2, Sort.by("age")))
        assertThat(firstPage.content).contains(youngest1, youngest2)

        val secondPage = repository.findTop3UsersBy(PageRequest.of(1, 2, Sort.by("age")))
        assertThat(secondPage.content).contains(youngest3)
    }

    @Test
    fun find2YoungestUsersPageableWithPageSize3Sliced() {
        flushTestUsers()

        val youngest1 = secondUser
        val youngest2 = thirdUser
        val youngest3 = fourthUser

        val firstPage = repository.findTop2UsersBy(PageRequest.of(0, 3, Sort.by("age")))
        assertThat(firstPage.content).contains(youngest1, youngest2)

        val secondPage = repository.findTop2UsersBy(PageRequest.of(1, 3, Sort.by("age")))
        assertThat(secondPage.content).contains(youngest3)
    }

    @Test
    fun pageableQueryReportsTotalFromResult() {

        flushTestUsers()

        val firstPage = repository.findAll(PageRequest.of(0, 10))
        assertThat(firstPage.content).hasSize(4)
        assertThat(firstPage.totalElements).isEqualTo(4L)

        val secondPage = repository.findAll(PageRequest.of(1, 3))
        assertThat(secondPage.content).hasSize(1)
        assertThat(secondPage.totalElements).isEqualTo(4L)
    }

    @Test
    fun pageableQueryReportsTotalFromCount() {

        flushTestUsers()

        val firstPage = repository.findAll(PageRequest.of(0, 4))
        assertThat(firstPage.content).hasSize(4)
        assertThat(firstPage.totalElements).isEqualTo(4L)

        val secondPage = repository.findAll(PageRequest.of(10, 10))
        assertThat(secondPage.content).hasSize(0)
        assertThat(secondPage.totalElements).isEqualTo(4L)
    }

    @Test
    fun invokesQueryWithWrapperType() {

        flushTestUsers()

        // HINT: Query by property 에서는 ReturnedType 에 맞게 변형해준다. Query by Raw Query 에서 이 것을 참조해서 변경해야 한다.
        val result = repository.findOptionalByEmailAddress("debop@example.com")

        assertThat(result.isPresent).isEqualTo(true)
        assertThat(result.get()).isEqualTo(firstUser)
    }

    // NOTE: We will not support Spring Expression !!!

    @Test
    fun findByEmptyCollectionOfIntegers() {

        flushTestUsers()

        val users = repository.findByAgeIn(emptyList())
        assertThat(users).isEmpty()
    }

    @Test
    fun findByEmptyArrayOfIntegers() {

        flushTestUsers()

        val users = repository.queryByAgeIn(arrayOf())
        assertThat(users).isEmpty()
    }

    @Test
    fun findByAgeWithEmptyArrayOfIntegersOrFirstName() {

        flushTestUsers()

        val users = repository.queryByAgeInOrFirstname(arrayOf(), secondUser.firstname)
        assertThat(users).hasSize(1).containsOnly(secondUser)
    }

    @Test
    fun shouldSupportJava8StreamsForRepositoryFinderMethods() {

        flushTestUsers()

        repository.findAllByCustomQueryAndStream().use { stream -> assertThat(stream).hasSize(4) }
    }

    @Test
    fun shouldSupportJava8StreamsForRepositoryDerivedFinderMethods() {

        flushTestUsers()

        repository.readAllByFirstnameNotNull().use { stream -> assertThat(stream).hasSize(4) }
    }

    @Test
    fun supportsJava8StreamForPageableMethod() {

        flushTestUsers()

        repository.streamAllPaged(PageRequest.of(0, 2)).use { stream -> assertThat(stream).hasSize(2) }
    }

    @Test
    fun findAllByExample() {

        flushTestUsers()

        val prototype = createUser()
        prototype.age = 51
        prototype.createdAt = null

        // FIXME : active 에 대해서는 filtering 되지만, age에 대해서는 filtering 이 생성되지 않는다.

        val users = repository.findAll(Example.of(prototype))

        assertThat(users).hasSize(1).containsOnly(firstUser)
    }

    @Test
    fun findAllByExampleWithEmptyProbe() {

        flushTestUsers()

        val prototype = createUser()
        prototype.createdAt = null

        val users = repository.findAll(Example.of(prototype, matching().withIgnorePaths("age", "createdAt", "active")))

        assertThat(users).hasSize(4)
    }

    @Test
    fun findAllByExampleWithExcludedAttributes() {

        flushTestUsers()

        val prototype = createUser()
        prototype.age = 51

        val example = Example.of(prototype, matching().withIgnorePaths("createdAt"))

        val users = repository.findAll(example)

        assertThat(users).containsOnly(firstUser)
    }

    @Test
    fun findAllByExampleWithStartingStringMatcher() {

        flushTestUsers()

        val prototype = createUser()
        prototype.firstname = "De"
        val example = Example.of(prototype,
                                 matching()
                                     .withStringMatcher(ExampleMatcher.StringMatcher.STARTING)
                                     .withIgnorePaths("age", "createdAt"))

        val users = repository.findAll(example)

        assertThat(users).containsOnly(firstUser)
    }

    @Test
    fun findAllByExampleWithEndingStringMatcher() {

        flushTestUsers()

        val prototype = createUser()
        prototype.firstname = "op"
        val example = Example.of(prototype,
                                 matching()
                                     .withStringMatcher(ExampleMatcher.StringMatcher.ENDING)
                                     .withIgnorePaths("age", "createdAt"))

        val users = repository.findAll(example)

        assertThat(users).containsOnly(firstUser)
    }

    @Test(expected = IllegalArgumentException::class)
    fun findAllByExampleWithRegexStringMatcher() {

        flushTestUsers()

        val prototype = createUser()
        prototype.firstname = "^Debop$"
        val example = Example.of(prototype,
                                 matching().withStringMatcher(ExampleMatcher.StringMatcher.REGEX))

        val users = repository.findAll(example)
        assertTrue { users.isNotEmpty() }
    }

    @Test
    fun findAllByExampleWithIgnoreCase() {

        flushTestUsers()

        val prototype = createUser()
        prototype.firstname = "dEBoP"
        val example = Example.of(prototype,
                                 matching().withIgnoreCase().withIgnorePaths("age", "createdAt"))

        val users = repository.findAll(example)
        assertThat(users).containsOnly(firstUser)
    }

    @Test
    fun findAllByExampleWithStringMatcherAndIgnoreCase() {
        flushTestUsers()

        val prototype = createUser()
        prototype.firstname = "dEB"
        val example = Example.of(prototype,
                                 matching()
                                     .withStringMatcher(ExampleMatcher.StringMatcher.STARTING)
                                     .withIgnoreCase()
                                     .withIgnorePaths("age", "createdAt"))

        val users = repository.findAll(example)
        assertThat(users).containsOnly(firstUser)
    }

    @Test
    fun findAllByExampleWithIncludeNull() {

        flushTestUsers()

        firstUser.dateOfBirth = Date()

        val fifthUser = createUser(firstUser.firstname, firstUser.lastname, "foo@bar.com")
        fifthUser.active = firstUser.active
        fifthUser.age = firstUser.age

        repository.saveAll(Arrays.asList(firstUser, fifthUser))

        val prototype = createUser()
        prototype.firstname = firstUser.firstname

        val example = Example.of(prototype,
                                 matching()
                                     .withIncludeNullValues()
                                     .withIgnorePaths("id", "binaryData", "lastname", "emailAddress", "age", "createdAt"))

        val users = repository.findAll(example)
        assertThat(users).contains(fifthUser)
    }

    @Test
    fun findAllByExampleWithPropertySpecifier() {

        flushTestUsers()

        val prototype = createUser()
        prototype.firstname = "dEb"

        val example = Example.of(prototype,
                                 matching()
                                     .withIgnoreCase()
                                     .withIgnorePaths("age", "createdAt")
                                     .withMatcher("firstname", ExampleMatcher.GenericPropertyMatcher().startsWith()))

        val users = repository.findAll(example)
        assertThat(users).contains(firstUser)
    }

    @Test
    fun findAllByExampleWithSort() {

        flushTestUsers()

        val user1 = createUser("Debop", "Spring", "d@c.kr")
        user1.age = 30
        repository.save(user1)

        val prototype = createUser()
        prototype.firstname = "dEb"

        val example = Example.of(prototype,
                                 matching()
                                     .withIgnoreCase()
                                     .withIgnorePaths("age", "createdAt")
                                     .withStringMatcher(ExampleMatcher.StringMatcher.STARTING))

        val users = repository.findAll(example, Sort.by(Sort.Direction.DESC, "age"))
        assertThat(users).contains(firstUser, user1)
    }

    @Test
    fun findAllByExampleWithPageable() {

        flushTestUsers()

        val count = 99
        for(i in 0 until count) {
            val user = createUser("Debop-$i", "Spring", "debop-$i@example.org")
            user.age = 100 + i
            repository.save(user)
        }

        val prototype = createUser()
        prototype.firstname = "dEb"

        val example = Example.of(prototype,
                                 matching()
                                     .withIgnoreCase()
                                     .withIgnorePaths("age", "createdAt")
                                     .withStringMatcher(ExampleMatcher.StringMatcher.STARTING))

        val users = repository.findAll(example, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "age")))

        assertThat(users.getSize()).isEqualTo(10)
        assertThat(users.hasNext()).isTrue()
        assertThat(users.getTotalElements()).isEqualTo((count + 1).toLong())
    }

    // NOTE: Association 조회를 지원하지 않기 때문에, 검증조차 하지 않습니다 ^^
    @Test // (expected = IllegalArgumentException.class)
    fun findAllByExampleShouldNotAllowCycles() {

        flushTestUsers()

        val prototype = createUser("user1", "", "")
        prototype.manager = prototype

        val example = Example.of(prototype,
                                 matching()
                                     .withIgnoreCase()
                                     .withIgnorePaths("age", "createdAt")
                                     .withStringMatcher(ExampleMatcher.StringMatcher.STARTING))

        repository.findAll(example)
    }

    @Test
    fun findOneByExampleWithExcludedAttributes() {

        flushTestUsers()

        val prototype = createUser()
        prototype.age = firstUser.age

        val example = Example.of(prototype, matching().withIgnorePaths("createdAt"))

        assertThat(repository.findOne(example)).contains(firstUser)
    }

    @Test
    fun countByExampleWithExcludedAttributes() {

        flushTestUsers()

        val prototype = createUser()
        prototype.age = firstUser.age

        // FIXME : active 에 대해서는 filtering 되지만, age에 대해서는 filtering 이 생성되지 않는다.
        val example = Example.of(prototype, matching().withIgnorePaths("createdAt"))

        val count = repository.count(example)

        assertThat(count).isEqualTo(1L)
    }

    @Test
    fun existsByExampleWithExcludedAttributes() {

        flushTestUsers()

        val prototype = createUser()
        prototype.age = firstUser.age
        val example = Example.of(prototype, matching().withIgnorePaths("createdAt"))

        val exists = repository.exists(example)

        assertThat(exists).isTrue()
    }

    @Test
    fun executesPagedWhereClauseAnOrder() {

        flushTestUsers()

        val filter = operations
            .select(UserEntity::class)
            .where(UserEntity.LASTNAME.like("%e%"))
            .orderBy<String>(UserEntity.LASTNAME.asc())
            .unwrap()

        val result = repository.findAll(filter, PageRequest.of(0, 1))

        assertThat(result.totalElements).isEqualTo(2L)
        assertThat(result.numberOfElements).isEqualTo(1)
        assertThat(result.content[0]).isEqualTo(firstUser)
    }

    // TODO: Raw String에서는 수형검사를 수행하지 않는다. (requery에서 지원하지 않는다)
    @Test
    fun exceptionsDuringParameterSettingGetThrown() {

        val users = repository.findByStringAge("twelve")
        assertThat(users).isEmpty()
    }

    @Test
    fun dynamicProjectionReturningStream() {

        flushTestUsers()

        assertThat(repository.findAsStreamByFirstnameLike("%De%", UserEntity::class.java)).hasSize(1)
    }

    @Test
    fun dynamicProjectionReturningList() {

        flushTestUsers()

        val users = repository.findAsListByFirstnameLike("%De%", UserEntity::class.java)
        assertThat(users).hasSize(1)
    }

    @Ignore("Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.")
    @Test
    fun supportsProjectionsWithNativeQueries() {

        flushTestUsers()

        val user = repository.findAll()[0]

        // TODO: Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.
        val result = repository.findByNativeQuery(user.id)!!

        assertThat(result.firstname).isEqualTo(user.firstname)
        assertThat(result.lastname).isEqualTo(user.lastname)
    }

    @Ignore("Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.")
    @Test
    fun supportsProjectionsWithNativeQueriesAndCamelCaseProperty() {

        flushTestUsers()

        val user = repository.findAll()[0]

        // TODO: Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.
        val result = repository.findEmailOnlyByNativeQuery(user.id)

        val emailAddress = result!!.emailAddress

        assertThat(emailAddress)
            .isEqualTo(user.emailAddress)
            .`as`("ensuring email is actually not null")
            .isNotNull()
    }

    @Test
    fun handlesColonsFollowedByIntegerInStringLiteral() {

        val firstName = "aFirstName"

        val expected = createUser(firstName, "000:1", "something@something")
        val notExpected = createUser(firstName, "000\\:1", "something@something.else")

        repository.save(expected)
        repository.save(notExpected)

        assertThat(repository.findAll()).hasSize(2)

        val users = repository.queryWithIndexedParameterAndColonFollowedByIntegerInString(firstName)

        assertThat(users).extracting<Int> { it.id }.containsExactly(expected.id)
    }

    @Test // DATAJPA-1233
    fun handlesCountQueriesWithLessParametersSingleParam() {
        repository.findAllOrderedBySpecialNameSingleParam("Debop", PageRequest.of(2, 3))
    }

    @Test // DATAJPA-1233
    fun handlesCountQueriesWithLessParametersMoreThanOne() {
        repository.findAllOrderedBySpecialNameMultipleParams("x", "Debop", PageRequest.of(2, 3))
    }

    @Test // DATAJPA-1233
    fun handlesCountQueriesWithLessParametersMoreThanOneIndexed() {
        repository.findAllOrderedBySpecialNameMultipleParamsIndexed("x", "Debop", PageRequest.of(2, 3))
    }

    @Test
    fun executeNativeQueryWithPage() {

        flushTestUsers()

        val firstPage = repository.findByNativQueryWithPageable(PageRequest.of(0, 3))
        val secondPage = repository.findByNativQueryWithPageable(PageRequest.of(1, 3))

        assertThat(firstPage.totalElements).isEqualTo(4)
        assertThat(firstPage.numberOfElements).isEqualTo(3)
        assertThat(firstPage.content)
            .extracting<String> { it.firstname }
            .containsExactly("Debop", "Diego", "Jinie")

        assertThat(secondPage.totalElements).isEqualTo(4)
        assertThat(secondPage.numberOfElements).isEqualTo(1)
        assertThat(secondPage.content)
            .extracting<String> { it.firstname }
            .containsExactly("Nickoon")
    }

    @Ignore("아직 List<Tuple> 을 원하는 수형으로 변환하는 기능을 제공하지 않습니다.")
    @Test
    fun executeNativeQueryWithPageWorkaround() {

        flushTestUsers()

        val firstPage = repository.findAsStringByNativeQueryWithPageable(PageRequest.of(0, 3))
        val secondPage = repository.findAsStringByNativeQueryWithPageable(PageRequest.of(1, 3))

        assertThat(firstPage.totalElements).isEqualTo(4)
        assertThat(firstPage.numberOfElements).isEqualTo(3)
        assertThat(firstPage.content)
            .containsExactly("Debop", "Diego", "Jinie")

        assertThat(secondPage.totalElements).isEqualTo(4)
        assertThat(secondPage.numberOfElements).isEqualTo(1)
        assertThat(secondPage.content)
            .containsExactly("Nickoon")
    }

    @Test // DATAJPA-1301
    fun returnsNullValueInMap() {

        firstUser.lastname = null
        flushTestUsers()

        val tuple = repository.findTupleWithNullValues()

        val softly = SoftAssertions()

        softly.assertThat(tuple.get<String>("firstname")).isEqualTo("Debop")
        softly.assertThat(tuple.get<String>("lastname")).isNull()

        softly.assertThat(tuple.get<String>("non-existent")).isNull()

        softly.assertAll()
    }

    @Test // DATAJPA-1307
    fun testFindByEmailAddressJdbcStyleParameter() {

        flushTestUsers()

        val user = repository.findByEmailNativeAddressJdbcStyleParameter("debop@example.com")
        assertThat(user).isEqualTo(firstUser)
    }

    private fun executeSpecWithSort(sort: Sort): Page<UserEntity> {

        flushTestUsers()

        val filter = operations
            .select(UserEntity::class)
            .where(UserEntity.FIRSTNAME.eq("Debop"))
            .or(UserEntity.LASTNAME.eq("Park"))
            .unwrap()

        val result = repository.findAll(filter,
                                        PageRequest.of(0, 1, sort))

        assertThat(result.totalElements).isEqualTo(2L)
        return result
    }

}