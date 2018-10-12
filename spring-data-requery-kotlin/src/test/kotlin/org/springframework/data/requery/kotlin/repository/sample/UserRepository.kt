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

package org.springframework.data.requery.kotlin.repository.sample

import io.requery.kotlin.eq
import io.requery.query.Tuple
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.Param
import org.springframework.data.requery.kotlin.annotation.Query
import org.springframework.data.requery.kotlin.domain.sample.Role
import org.springframework.data.requery.kotlin.domain.sample.RoleEntity
import org.springframework.data.requery.kotlin.domain.sample.SpecialUer
import org.springframework.data.requery.kotlin.domain.sample.UserEntity
import org.springframework.data.requery.kotlin.domain.sample.UserEntity_RoleEntity
import org.springframework.data.requery.kotlin.domain.sample.User_Colleagues
import org.springframework.data.requery.kotlin.repository.RequeryRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

/**
 * UserRepository
 *
 * @author debop
 */
@SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
interface UserRepository : RequeryRepository<UserEntity, Int>, UserRepositoryCustom {

    fun findByLastname(lastname: String?): List<UserEntity>

    @Transactional
    override fun findById(primaryKey: Int): Optional<UserEntity>

    override fun deleteById(id: Int)

    fun findByEmailAddress(emailAddress: String): UserEntity?

    @Query("select u.* from SD_User u left outer join SD_User m on (u.manager = m.id)")
    fun findAllPaged(pageable: Pageable): Page<UserEntity>

    fun findByEmailAddressAndLastname(emailAddress: String, lastname: String): UserEntity?

    fun findByEmailAddressAndLastnameOrFirstname(emailAddress: String, lastname: String, firstname: String): List<UserEntity>

    @Query("select * from SD_User u where u.emailAddress = ?")
    @Transactional(readOnly = true)
    fun findByAnnotatedQuery(emailAddress: String): UserEntity?


    /**
     * Method to directly create query from and adding a [Pageable] parameter to be regarded on query execution.
     */
    fun findByLastname(pageable: Pageable, lastname: String): Page<UserEntity>

    /**
     * Method to directly create query from and adding a [Pageable] parameter to be regarded on query execution.
     * Just returns the queried [Page]'s contents.
     */
    fun findByFirstname(firstname: String, pageable: Pageable): List<UserEntity>

    fun findByFirstnameIn(pageable: Pageable, vararg firstnames: String): Page<UserEntity>

    fun findByFirstnameNotIn(firstname: Collection<String>): List<UserEntity>

    @Query("select * from SD_User u where u.firstname like ?")
    fun findByFirstnameLike(firstname: String): List<UserEntity>

    // NOTE: Not supported Named Parameter
    @Query("select * from SD_User u where u.firstname like :firstname%")
    fun findByFirstnameLikeNamed(@Param("firstname") firstname: String): List<UserEntity>

    /**
     * Manipulating query to set all [UserEntity]'s names to the given one.
     */
    @Query("update SD_User u set u.lastname = ?")
    fun renameAllUsersTo(lastname: String)

    @Query("select count(*) from SD_User u where u.firstname = ?")
    fun countWithFirstname(firstname: String): Long?

    @Query("select * from SD_User u where u.lastname = ? or u.firstname = ?")
    fun findByLastnameOrFirstname(/*@Param("firstname") */lastname: String, /*@Param("lastname") */firstname: String): List<UserEntity>

    @Query("select * from SD_User u where u.firstname = ? or u.lastname = ?")
    fun findByLastnameOrFirstnameUnannotated(firstname: String, lastname: String): List<UserEntity>

    fun findByFirstnameOrLastname(firstname: String, lastname: String): List<UserEntity>

    fun findByLastnameLikeOrderByFirstnameDesc(lastname: String): List<UserEntity>

    fun findByLastnameNotLike(lastname: String): List<UserEntity>

    fun findByLastnameNot(lastname: String): List<UserEntity>

    // NOTE: Not supported associated query, use direct join query instead.
    fun findByManagerLastname(name: String): List<UserEntity>

    // NOTE: Not supported associated query, use direct join query instead.
    fun findByColleaguesLastname(lastname: String): List<UserEntity>

    fun findByLastnameNotNull(): List<UserEntity>

    fun findByLastnameNull(): List<UserEntity>

    fun findByEmailAddressLike(email: String, sort: Sort): List<UserEntity>

    fun findSpecialUsersByLastname(lastname: String): List<SpecialUer>

    //    List<UserEntity> findBySpringDataNamedQuery(String lastname);

    fun findByLastnameIgnoringCase(lastname: String): List<UserEntity>

    fun findByLastnameIgnoringCase(pageable: Pageable, lastname: String): Page<UserEntity>

    fun findByLastnameIgnoringCaseLike(lastname: String): List<UserEntity>

    fun findByLastnameAndFirstnameAllIgnoringCase(lastname: String, firstname: String): List<UserEntity>

    fun findByAgeGreaterThanEqual(age: Int): List<UserEntity>

    fun findByAgeLessThanEqual(age: Int): List<UserEntity>

    @Query("select u.lastname from SD_User u group by u.lastname ")
    fun findByLastnameGrouped(pageable: Pageable): Page<String>

    // DATAJPA-117
    @Query(value = "SELECT * FROM SD_User WHERE lastname = ?1")
    fun findNativeByLastname(lastname: String): List<UserEntity>

    // DATAJPA-132
    fun findByActiveTrue(): List<UserEntity>

    // DATAJPA-132
    fun findByActiveFalse(): List<UserEntity>

    // HINT: @Query 를 쓰던가, requery api 를 사용하던가 같다.
    // @Query("select u.* from SD_User u inner join User_Colleagues uc on (u.id = uc.SD_UserId1) where uc.SD_UserId2 = ?")
    @JvmDefault
    fun findColleaguesFor(userId: Int?): List<UserEntity> {
        return operations
            .select(UserEntity::class)
            .join(User_Colleagues::class).on(UserEntity::id eq User_Colleagues.USER_ID)
            .where(User_Colleagues.FRIEND_ID.eq(userId))
            .get()
            .toList()
    }

    // DATAJPA-188
    fun findByCreatedAtBefore(date: Date): List<UserEntity>

    // DATAJPA-188
    fun findByCreatedAtAfter(date: Date): List<UserEntity>

    // DATAJPA-180
    fun findByFirstnameStartingWith(firstname: String): List<UserEntity>

    // DATAJPA-180
    fun findByFirstnameEndingWith(firstname: String): List<UserEntity>

    // DATAJPA-180
    fun findByFirstnameContaining(firstname: String): List<UserEntity>

    @Query(value = "SELECT 1 FROM SD_User")
    fun findOnesByNativeQuery(): List<Tuple>

    // DATAJPA-231
    fun countByLastname(lastname: String): Long

    // DATAJPA-231
    fun countUsersByFirstname(firstname: String): Int

    // DATAJPA-920
    fun existsByLastname(lastname: String): Boolean

    // DATAJPA-391
    @Query("select u.firstname from SD_User u where u.lastname = ?")
    fun findFirstnamesByLastname(lastname: String): List<String>

    // DATAJPA-415
    fun findByIdIn(vararg ids: Int): Collection<UserEntity>

    // DATAJPA-461
    @Query("select * from SD_User u where u.id in ?")
    fun findByIdsCustomWithPositionalVarArgs(vararg ids: Int): Collection<UserEntity>

    // DATAJPA-461
    @Query("select * from SD_User u where u.id in ?")
    fun findByIdsCustomWithNamedVarArgs(vararg ids: Int): Collection<UserEntity>


    // NOTE: Not supported Spring expression
    // DATAJPA-415

    @Query("update SD_User u set u.active = ? where u.id in ?")
    fun updateUserActiveState(activeState: Boolean, vararg ids: Int)

    // DATAJPA-405
    fun findAllByOrderByLastnameAsc(): List<UserEntity>

    // DATAJPA-454
    fun findByBinaryData(data: ByteArray): List<UserEntity>

    // DATAJPA-486
    fun findSliceByLastname(lastname: String, pageable: Pageable): Slice<UserEntity>

    //    // DATAJPA-496
    //    List<UserEntity> findByAttributesIn(Set<String> attributes);

    // DATAJPA-460
    fun removeByLastname(lastname: String): Int?

    // DATAJPA-460
    fun deleteByLastname(lastname: String): Int?


    @Query(value = "select * from SD_User u where u.firstname like ?")
    fun findAllByFirstnameLike(firstname: String, page: Pageable): Page<UserEntity>


    fun findFirstByOrderByAgeDesc(): UserEntity?

    fun findFirst1ByOrderByAgeDesc(): UserEntity?

    fun findTopByOrderByAgeDesc(): UserEntity?

    fun findTopByOrderByAgeAsc(): UserEntity?

    fun findTop1ByOrderByAgeAsc(): UserEntity?

    fun findTop2ByOrderByAgeDesc(): List<UserEntity>

    fun findFirst2ByOrderByAgeDesc(): List<UserEntity>

    fun findFirst2UsersBy(sort: Sort): List<UserEntity>

    fun findTop2UsersBy(sort: Sort): List<UserEntity>

    fun findFirst3UsersBy(page: Pageable): Page<UserEntity>

    fun findFirst2UsersBy(page: Pageable): Page<UserEntity>

    fun findTop3UsersBy(page: Pageable): Slice<UserEntity>

    fun findTop2UsersBy(page: Pageable): Slice<UserEntity>


    @Query("select u.binaryData from SD_User u where u.id = ?")
    fun findBinaryDataByIdNative(id: Int?): ByteArray

    @Query("select * from SD_User u where u.emailAddress = ?")
    fun findOptionalByEmailAddress(emailAddress: String): Optional<UserEntity>

    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = ?#{[0]} and u.firstname = ?1 and u.lastname like %?#{[1]}% and u.lastname like %?2%")
    //    List<UserEntity> findByFirstnameAndLastnameWithSpelExpression(String firstname, String lastname);
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.lastname like %:#{[0]}% and u.lastname like %:lastname%")
    //    List<UserEntity> findByLastnameWithSpelExpression(@Param("lastname") String lastname);
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = ?#{'Oliver'}")
    //    List<UserEntity> findOliverBySpELExpressionWithoutArgumentsWithQuestionmark();
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = :#{'Oliver'}")
    //    List<UserEntity> findOliverBySpELExpressionWithoutArgumentsWithColon();
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.age = ?#{[0]}")
    //    List<UserEntity> findUsersByAgeForSpELExpressionByIndexedParameter(int age);
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = :firstname and u.firstname = :#{#firstname}")
    //    List<UserEntity> findUsersByFirstnameForSpELExpression(@Param("firstname") String firstname);
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.emailAddress = ?#{principal.emailAddress}")
    //    List<UserEntity> findCurrentUserWithCustomQuery();
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = ?1 and u.firstname=?#{[0]} and u.emailAddress = ?#{principal.emailAddress}")
    //    List<UserEntity> findByFirstnameAndCurrentUserWithCustomQuery(String firstname);
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = :#{#firstname}")
    //    List<UserEntity> findUsersByFirstnameForSpELExpressionWithParameterVariableOnly(@Param("firstname") String firstname);
    //
    //    // DATAJPA-564
    //    @Query("select u from UserEntity u where u.firstname = ?#{[0]}")
    //    List<UserEntity> findUsersByFirstnameForSpELExpressionWithParameterIndexOnly(String firstname);
    //
    //    // DATAJPA-564
    //    @Query(
    //        value = "select * from (" +
    //                "select u.*, rownum() as RN from (" +
    //                "select * from SD_User ORDER BY ucase(firstname)" +
    //                ") u" +
    //                ") where RN between ?#{ #pageable.offset +1 } and ?#{#pageable.offset + #pageable.pageSize}",
    //        countQuery = "select count(u.id) from SD_User u", nativeQuery = true)
    //    Page<UserEntity> findUsersInNativeQueryWithPagination(Pageable pageable);
    //
    //    // DATAJPA-1140
    //    @Query("select u from UserEntity u where u.firstname =:#{#user.firstname} and u.lastname =:lastname")
    //    List<UserEntity> findUsersByUserFirstnameAsSpELExpressionAndLastnameAsString(@Param("user") UserEntity user,
    //                                                                           @Param("lastname") String lastname);
    //
    //    // DATAJPA-1140
    //    @Query("select u from UserEntity u where u.firstname =:firstname and u.lastname =:#{#user.lastname}")
    //    List<UserEntity> findUsersByFirstnameAsStringAndUserLastnameAsSpELExpression(@Param("firstname") String firstname,
    //                                                                           @Param("user") UserEntity user);
    //
    //    // DATAJPA-1140
    //    @Query("select u from UserEntity u where u.firstname =:#{#user.firstname} and u.lastname =:#{#lastname}")
    //    List<UserEntity> findUsersByUserFirstnameAsSpELExpressionAndLastnameAsFakeSpELExpression(@Param("user") UserEntity user,
    //                                                                                       @Param("lastname") String lastname);
    //
    //    // DATAJPA-1140
    //    @Query("select u from UserEntity u where u.firstname =:#{#firstname} and u.lastname =:#{#user.lastname}")
    //    List<UserEntity> findUsersByFirstnameAsFakeSpELExpressionAndUserLastnameAsSpELExpression(
    //        @Param("firstname") String firstname, @Param("user") UserEntity user);
    //
    //    // DATAJPA-1140
    //    @Query("select u from UserEntity u where u.firstname =:firstname")
    //    List<UserEntity> findUsersByFirstnamePaginated(Pageable page, @Param("firstname") String firstname);
    //
    //    // DATAJPA-629
    //    @Query("select u from #{#entityName} u where u.firstname = ?#{[0]} and u.lastname = ?#{[1]}")
    //    List<UserEntity> findUsersByFirstnameForSpELExpressionWithParameterIndexOnlyWithEntityExpression(String firstname,
    //                                                                                               String lastname);

    fun findByAgeIn(ages: Collection<Int>): List<UserEntity>

    fun queryByAgeIn(ages: Array<Int>): List<UserEntity>

    fun queryByAgeInOrFirstname(ages: Array<Int>, firstname: String): List<UserEntity>

    @Query("select * from SD_User")
    fun findAllByCustomQueryAndStream(): Stream<UserEntity>

    fun readAllByFirstnameNotNull(): Stream<UserEntity>

    @Query("select * from SD_User")
    fun streamAllPaged(pageable: Pageable): Stream<UserEntity>

    fun findByLastnameNotContaining(part: String): List<UserEntity>

    // NOTE: Not Supported

    @JvmDefault
    fun findByRolesContaining(role: Role): List<UserEntity> {
        return operations
            .select(UserEntity::class)
            .join(UserEntity_RoleEntity::class).on(UserEntity_RoleEntity.SD_USER_ID.eq(UserEntity.ID)
                                                       .and(UserEntity_RoleEntity.SD_ROLES_ID.eq(role.id)))
            .get()
            .toList()
    }

    @JvmDefault
    fun findByRolesNotContaining(role: Role): List<UserEntity> {
        return operations
            .select(UserEntity::class)
            .distinct()
            .leftJoin(UserEntity_RoleEntity::class).on(UserEntity_RoleEntity.SD_USER_ID.eq(UserEntity.ID))
            .where(UserEntity_RoleEntity.SD_ROLES_ID.ne(role.id).or(UserEntity_RoleEntity.SD_ROLES_ID.isNull()))
            .get()
            .toList()
    }

    @JvmDefault
    fun findByRolesNameContaining(roleName: String): List<UserEntity> {
        return operations
            .select(UserEntity::class)
            .join(UserEntity_RoleEntity::class).on(UserEntity_RoleEntity.SD_USER_ID.eq(UserEntity.ID))
            .join(RoleEntity::class).on(UserEntity_RoleEntity.SD_ROLES_ID.eq(RoleEntity.ID))
            .where(RoleEntity.NAME.eq(roleName))
            .get()
            .toList()
    }


    //    // DATAJPA-1179
    //    @Query("select u from UserEntity u where u.firstname = :#{#firstname} and u.firstname = :#{#firstname}")
    //    List<UserEntity> findUsersByDuplicateSpel(@Param("firstname") String firstname);
    //
    @JvmDefault
    fun findRolesAndFirstnameBy(): List<UserEntity> {
        return operations
            .select(UserEntity::class)
            .get()
            .toList()
    }

    @Query("select * from SD_User u where u.age = ?")
    fun findByStringAge(age: String): List<UserEntity>


    //    // DATAJPA-1185
    fun <T> findAsStreamByFirstnameLike(name: String, projectionType: Class<T>): Stream<T>

    //
    //    // DATAJPA-1185
    fun <T> findAsListByFirstnameLike(name: String, projectionType: Class<T>): List<T>

    @Query("SELECT u.firstname, u.lastname from SD_User u WHERE u.id=?")
    fun findByNativeQuery(id: Int?): NameOnly?

    //
    @Query("SELECT u.emailaddress from SD_User u WHERE u.id=?")
    fun findEmailOnlyByNativeQuery(id: Int?): EmailOnly?

    // NOTE: Not Supported

    //    // DATAJPA-1235
    @Query("SELECT u.* FROM SD_User u where u.firstname >= ? and u.lastname = '000:1'")
    fun queryWithIndexedParameterAndColonFollowedByIntegerInString(firstname: String): List<UserEntity>


    // DATAJPA-1233
    @Query(value = "SELECT u.* FROM SD_User u ORDER BY CASE WHEN (u.firstname  >= ?) THEN 0 ELSE 1 END, u.firstname")
    fun findAllOrderedBySpecialNameSingleParam(name: String, page: Pageable): Page<UserEntity>


    // DATAJPA-1233
    @Query(value = "SELECT u.* FROM SD_User u WHERE 'x' = ? ORDER BY CASE WHEN (u.firstname  >= ?) THEN 0 ELSE 1 END, u.firstname")
    fun findAllOrderedBySpecialNameMultipleParams(other: String, name: String, page: Pageable): Page<UserEntity>

    // DATAJPA-1233
    @Query(value = "SELECT u.* FROM SD_User u WHERE 'x' = ? ORDER BY CASE WHEN (u.firstname  >= ?) THEN 0 ELSE 1 END, u.firstname")
    fun findAllOrderedBySpecialNameMultipleParamsIndexed(other: String, name: String, page: Pageable): Page<UserEntity>

    //    // DATAJPA-928
    @Query(value = "SELECT u.* FROM SD_User u")
    fun findByNativQueryWithPageable(pageable: Pageable): Page<UserEntity>

    // DATAJPA-928
    @Query(value = "SELECT firstname FROM SD_User ORDER BY UCASE(firstname)", countQuery = "SELECT count(*) FROM SD_User")
    fun findAsStringByNativeQueryWithPageable(pageable: Pageable): Page<String>

    //    // DATAJPA-1273
    //    List<NameOnly> findByNamedQueryWithAliasInInvertedOrder();

    // DATAJPA-1301
    @Query("select u.firstname as firstname, u.lastname as lastname from SD_User u where u.firstname = 'Debop'")
    fun findTupleWithNullValues(): Tuple

    // DATAJPA-1307
    @Query(value = "select u.* from SD_User u where u.emailAddress = ?")
    fun findByEmailNativeAddressJdbcStyleParameter(emailAddress: String): UserEntity?

    // NOTE: Not Supported
    // DATAJPA-1334
    @JvmDefault
    fun findByNamedQueryWithConstructorExpression(): List<RolesAndFirstname> {
        return operations
            .select(UserEntity::class)
            .get()
            .toList()
            .map { it -> it as RolesAndFirstname }
    }

    interface RolesAndFirstname {

        val firstname: String

        val roles: Set<Role>
    }


    interface NameOnly {

        val firstname: String

        val lastname: String
    }

    interface EmailOnly {

        val emailAddress: String
    }

}