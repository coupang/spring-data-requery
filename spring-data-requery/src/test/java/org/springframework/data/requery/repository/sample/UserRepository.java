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

package org.springframework.data.requery.repository.sample;

import io.requery.query.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.domain.sample.AbstractRole;
import org.springframework.data.requery.domain.sample.Role;
import org.springframework.data.requery.domain.sample.SpecialUser;
import org.springframework.data.requery.domain.sample.User;
import org.springframework.data.requery.domain.sample.User_Colleagues;
import org.springframework.data.requery.domain.sample.User_Role;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * UserRepository
 *
 * @author debop
 * @since 18. 6. 12
 */
@SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
@Transactional(readOnly = true)
public interface UserRepository extends RequeryRepository<User, Integer>, UserRepositoryCustom {

    List<User> findByLastname(String lastname);

    @Override
    Optional<User> findById(Integer primaryKey);

    @Transactional
    @Override
    void deleteById(Integer id);

    User findByEmailAddress(String emailAddress);

    @Query("select u.* from SD_User u left outer join SD_User m on (u.manager = m.id)")
    Page<User> findAllPaged(Pageable pageable);

    User findByEmailAddressAndLastname(String emailAddress, String lastname);

    List<User> findByEmailAddressAndLastnameOrFirstname(String emailAddress, String lastname, String firstname);

    @Query("select * from SD_User u where u.emailAddress = ?")
    User findByAnnotatedQuery(String emailAddress);

    /**
     * Method to directly create query from and adding a {@link Pageable} parameter to be regarded on query execution.
     */
    Page<User> findByLastname(Pageable pageable, String lastname);

    /**
     * Method to directly create query from and adding a {@link Pageable} parameter to be regarded on query execution.
     * Just returns the queried {@link Page}'s contents.
     */
    List<User> findByFirstname(String firstname, Pageable pageable);

    Page<User> findByFirstnameIn(Pageable pageable, String... firstnames);

    List<User> findByFirstnameNotIn(Collection<String> firstname);

    @Query("select * from SD_User u where u.firstname like ?")
    List<User> findByFirstnameLike(String firstname);

    // NOTE: Not supported Named Parameter
    @Query("select * from SD_User u where u.firstname like :firstname%")
    List<User> findByFirstnameLikeNamed(@Param("firstname") String firstname);

    /**
     * Manipulating query to set all {@link User}'s names to the given one.
     */
    @Transactional
    @Query("update SD_User u set u.lastname = ?")
    void renameAllUsersTo(String lastname);

    @Query("select count(*) from SD_User u where u.firstname = ?")
    Long countWithFirstname(String firstname);

    @Query("select * from SD_User u where u.lastname = ? or u.firstname = ?")
    List<User> findByLastnameOrFirstname(/*@Param("firstname") */String lastname, /*@Param("lastname") */String firstname);

    @Query("select * from SD_User u where u.firstname = ? or u.lastname = ?")
    List<User> findByLastnameOrFirstnameUnannotated(String firstname, String lastname);

    List<User> findByFirstnameOrLastname(String firstname, String lastname);

    List<User> findByLastnameLikeOrderByFirstnameDesc(String lastname);

    List<User> findByLastnameNotLike(String lastname);

    List<User> findByLastnameNot(String lastname);

    // NOTE: Not supported associated query, use direct join query instead.
    List<User> findByManagerLastname(String name);

    // NOTE: Not supported associated query, use direct join query instead.
    List<User> findByColleaguesLastname(String lastname);

    List<User> findByLastnameNotNull();

    List<User> findByLastnameNull();

    List<User> findByEmailAddressLike(String email, Sort sort);

    List<SpecialUser> findSpecialUsersByLastname(String lastname);

//    List<User> findBySpringDataNamedQuery(String lastname);

    List<User> findByLastnameIgnoringCase(String lastname);

    Page<User> findByLastnameIgnoringCase(Pageable pageable, String lastname);

    List<User> findByLastnameIgnoringCaseLike(String lastname);

    List<User> findByLastnameAndFirstnameAllIgnoringCase(String lastname, String firstname);

    List<User> findByAgeGreaterThanEqual(int age);

    List<User> findByAgeLessThanEqual(int age);

    @Query("select u.lastname from SD_User u group by u.lastname ")
    Page<String> findByLastnameGrouped(Pageable pageable);

    // DATAJPA-117
    @Query(value = "SELECT * FROM SD_User WHERE lastname = ?1")
    List<User> findNativeByLastname(String lastname);

    // DATAJPA-132
    List<User> findByActiveTrue();

    // DATAJPA-132
    List<User> findByActiveFalse();

    // HINT: @Query 를 쓰던가, requery api 를 사용하던가 같다.
    // @Query("select u.* from SD_User u inner join User_Colleagues uc on (u.id = uc.SD_UserId1) where uc.SD_UserId2 = ?")
    default List<User> findColleaguesFor(Integer userId) {
        return getOperations()
            .select(User.class)
            .join(User_Colleagues.class).on(User.ID.eq(User_Colleagues.SD_USER_ID1))
            .where(User_Colleagues.SD_USER_ID2.eq(userId))
            .get()
            .toList();
    }

    // DATAJPA-188
    List<User> findByCreatedAtBefore(Date date);

    // DATAJPA-188
    List<User> findByCreatedAtAfter(Date date);

    // DATAJPA-180
    List<User> findByFirstnameStartingWith(String firstname);

    // DATAJPA-180
    List<User> findByFirstnameEndingWith(String firstname);

    // DATAJPA-180
    List<User> findByFirstnameContaining(String firstname);

    @Query(value = "SELECT 1 FROM SD_User")
    List<Tuple> findOnesByNativeQuery();

    // DATAJPA-231
    long countByLastname(String lastname);

    // DATAJPA-231
    int countUsersByFirstname(String firstname);

    // DATAJPA-920
    boolean existsByLastname(String lastname);

    // DATAJPA-391
    @Query("select u.firstname from SD_User u where u.lastname = ?")
    List<String> findFirstnamesByLastname(String lastname);

    // DATAJPA-415
    Collection<User> findByIdIn(Integer... ids);

    // DATAJPA-461
    @Query("select * from SD_User u where u.id in ?")
    Collection<User> findByIdsCustomWithPositionalVarArgs(Integer... ids);

    // DATAJPA-461
    @Query("select * from SD_User u where u.id in ?")
    Collection<User> findByIdsCustomWithNamedVarArgs(Integer... ids);


    // NOTE: Not supported Spring expression
    // DATAJPA-415

    @Transactional
    @Query("update SD_User u set u.active = ? where u.id in ?")
    void updateUserActiveState(boolean activeState, Integer... ids);

    // DATAJPA-405
    List<User> findAllByOrderByLastnameAsc();

    // DATAJPA-454
    List<User> findByBinaryData(byte[] data);

    // DATAJPA-486
    Slice<User> findSliceByLastname(String lastname, Pageable pageable);

//    // DATAJPA-496
//    List<User> findByAttributesIn(Set<String> attributes);

    @Transactional
    Integer removeByLastname(String lastname);

    @Transactional
    Integer deleteByLastname(String lastname);

    @Query(value = "select * from SD_User u where u.firstname like ?")
    Page<User> findAllByFirstnameLike(String firstname, Pageable page);


    User findFirstByOrderByAgeDesc();

    User findFirst1ByOrderByAgeDesc();

    User findTopByOrderByAgeDesc();

    User findTopByOrderByAgeAsc();

    User findTop1ByOrderByAgeAsc();

    List<User> findTop2ByOrderByAgeDesc();

    List<User> findFirst2ByOrderByAgeDesc();

    List<User> findFirst2UsersBy(Sort sort);

    List<User> findTop2UsersBy(Sort sort);

    Page<User> findFirst3UsersBy(Pageable page);

    Page<User> findFirst2UsersBy(Pageable page);

    Slice<User> findTop3UsersBy(Pageable page);

    Slice<User> findTop2UsersBy(Pageable page);


    @Query("select u.binaryData from SD_User u where u.id = ?")
    byte[] findBinaryDataByIdNative(Integer id);

    @Query("select * from SD_User u where u.emailAddress = ?")
    Optional<User> findOptionalByEmailAddress(String emailAddress);

//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = ?#{[0]} and u.firstname = ?1 and u.lastname like %?#{[1]}% and u.lastname like %?2%")
//    List<User> findByFirstnameAndLastnameWithSpelExpression(String firstname, String lastname);
//
//    // DATAJPA-564
//    @Query("select u from User u where u.lastname like %:#{[0]}% and u.lastname like %:lastname%")
//    List<User> findByLastnameWithSpelExpression(@Param("lastname") String lastname);
//
//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = ?#{'Oliver'}")
//    List<User> findOliverBySpELExpressionWithoutArgumentsWithQuestionmark();
//
//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = :#{'Oliver'}")
//    List<User> findOliverBySpELExpressionWithoutArgumentsWithColon();
//
//    // DATAJPA-564
//    @Query("select u from User u where u.age = ?#{[0]}")
//    List<User> findUsersByAgeForSpELExpressionByIndexedParameter(int age);
//
//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = :firstname and u.firstname = :#{#firstname}")
//    List<User> findUsersByFirstnameForSpELExpression(@Param("firstname") String firstname);
//
//    // DATAJPA-564
//    @Query("select u from User u where u.emailAddress = ?#{principal.emailAddress}")
//    List<User> findCurrentUserWithCustomQuery();
//
//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = ?1 and u.firstname=?#{[0]} and u.emailAddress = ?#{principal.emailAddress}")
//    List<User> findByFirstnameAndCurrentUserWithCustomQuery(String firstname);
//
//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = :#{#firstname}")
//    List<User> findUsersByFirstnameForSpELExpressionWithParameterVariableOnly(@Param("firstname") String firstname);
//
//    // DATAJPA-564
//    @Query("select u from User u where u.firstname = ?#{[0]}")
//    List<User> findUsersByFirstnameForSpELExpressionWithParameterIndexOnly(String firstname);
//
//    // DATAJPA-564
//    @Query(
//        value = "select * from (" +
//                "select u.*, rownum() as RN from (" +
//                "select * from SD_User ORDER BY ucase(firstname)" +
//                ") u" +
//                ") where RN between ?#{ #pageable.offset +1 } and ?#{#pageable.offset + #pageable.pageSize}",
//        countQuery = "select count(u.id) from SD_User u", nativeQuery = true)
//    Page<User> findUsersInNativeQueryWithPagination(Pageable pageable);
//
//    // DATAJPA-1140
//    @Query("select u from User u where u.firstname =:#{#user.firstname} and u.lastname =:lastname")
//    List<User> findUsersByUserFirstnameAsSpELExpressionAndLastnameAsString(@Param("user") User user,
//                                                                           @Param("lastname") String lastname);
//
//    // DATAJPA-1140
//    @Query("select u from User u where u.firstname =:firstname and u.lastname =:#{#user.lastname}")
//    List<User> findUsersByFirstnameAsStringAndUserLastnameAsSpELExpression(@Param("firstname") String firstname,
//                                                                           @Param("user") User user);
//
//    // DATAJPA-1140
//    @Query("select u from User u where u.firstname =:#{#user.firstname} and u.lastname =:#{#lastname}")
//    List<User> findUsersByUserFirstnameAsSpELExpressionAndLastnameAsFakeSpELExpression(@Param("user") User user,
//                                                                                       @Param("lastname") String lastname);
//
//    // DATAJPA-1140
//    @Query("select u from User u where u.firstname =:#{#firstname} and u.lastname =:#{#user.lastname}")
//    List<User> findUsersByFirstnameAsFakeSpELExpressionAndUserLastnameAsSpELExpression(
//        @Param("firstname") String firstname, @Param("user") User user);
//
//    // DATAJPA-1140
//    @Query("select u from User u where u.firstname =:firstname")
//    List<User> findUsersByFirstnamePaginated(Pageable page, @Param("firstname") String firstname);
//
//    // DATAJPA-629
//    @Query("select u from #{#entityName} u where u.firstname = ?#{[0]} and u.lastname = ?#{[1]}")
//    List<User> findUsersByFirstnameForSpELExpressionWithParameterIndexOnlyWithEntityExpression(String firstname,
//                                                                                               String lastname);

    List<User> findByAgeIn(Collection<Integer> ages);

    List<User> queryByAgeIn(Integer[] ages);

    List<User> queryByAgeInOrFirstname(Integer[] ages, String firstname);

    @Query("select * from SD_User")
    Stream<User> findAllByCustomQueryAndStream();

    Stream<User> readAllByFirstnameNotNull();

    @Query("select * from SD_User")
    Stream<User> streamAllPaged(Pageable pageable);

    List<User> findByLastnameNotContaining(String part);

    // NOTE: Not Supported

    default List<User> findByRolesContaining(AbstractRole role) {
        return getOperations()
            .select(User.class)
            .join(User_Role.class).on(User_Role.SD_USER_ID.eq(User.ID).and(User_Role.SD_ROLES_ID.eq(role.getId())))
            .get()
            .toList();
    }

    default List<User> findByRolesNotContaining(AbstractRole role) {
        return getOperations()
            .select(User.class)
            .distinct()
            .leftJoin(User_Role.class).on(User_Role.SD_USER_ID.eq(User.ID))
            .where(User_Role.SD_ROLES_ID.ne(role.getId()).or(User_Role.SD_ROLES_ID.isNull()))
            .get()
            .toList();
    }

    default List<User> findByRolesNameContaining(String roleName) {
        return getOperations()
            .select(User.class)
            .join(User_Role.class).on(User_Role.SD_USER_ID.eq(User.ID))
            .join(Role.class).on(User_Role.SD_ROLES_ID.eq(Role.ID))
            .where(Role.NAME.eq(roleName))
            .get()
            .toList();
    }


    //    // DATAJPA-1179
//    @Query("select u from User u where u.firstname = :#{#firstname} and u.firstname = :#{#firstname}")
//    List<User> findUsersByDuplicateSpel(@Param("firstname") String firstname);
//
    default List<User> findRolesAndFirstnameBy() {
        return getOperations()
            .select(User.class)
            .get()
            .toList();
    }

    @Query("select * from SD_User u where u.age = ?")
    List<User> findByStringAge(String age);


    <T> Stream<T> findAsStreamByFirstnameLike(String name, Class<T> projectionType);

    <T> List<T> findAsListByFirstnameLike(String name, Class<T> projectionType);

    @Query("SELECT u.firstname, u.lastname from SD_User u WHERE u.id=?")
    NameOnly findByNativeQuery(Integer id);

    @Query("SELECT u.emailaddress from SD_User u WHERE u.id=?")
    EmailOnly findEmailOnlyByNativeQuery(Integer id);

    // NOTE: Not Supported

    @Query("SELECT u.* FROM SD_User u where u.firstname >= ? and u.lastname = '000:1'")
    List<User> queryWithIndexedParameterAndColonFollowedByIntegerInString(String firstname);

    @Query(value = "SELECT u.* FROM SD_User u ORDER BY CASE WHEN (u.firstname  >= ?) THEN 0 ELSE 1 END, u.firstname")
    Page<User> findAllOrderedBySpecialNameSingleParam(String name, Pageable page);

    @Query(value = "SELECT u.* FROM SD_User u WHERE 'x' = ? ORDER BY CASE WHEN (u.firstname  >= ?) THEN 0 ELSE 1 END, u.firstname")
    Page<User> findAllOrderedBySpecialNameMultipleParams(String other, String name, Pageable page);

    @Query(value = "SELECT u.* FROM SD_User u WHERE 'x' = ? ORDER BY CASE WHEN (u.firstname  >= ?) THEN 0 ELSE 1 END, u.firstname")
    Page<User> findAllOrderedBySpecialNameMultipleParamsIndexed(String other, String name, Pageable page);

    @Query(value = "SELECT * FROM SD_User")
    Page<User> findByNativQueryWithPageable(Pageable pageable);

    @Query(value = "SELECT firstname FROM SD_User ORDER BY UCASE(firstname)", countQuery = "SELECT count(*) FROM SD_User")
    Page<String> findAsStringByNativeQueryWithPageable(Pageable pageable);

//    // DATAJPA-1273
//    List<NameOnly> findByNamedQueryWithAliasInInvertedOrder();

    // DATAJPA-1301
    @Query("select u.firstname as firstname, u.lastname as lastname from SD_User u where u.firstname = 'Debop'")
    Tuple findTupleWithNullValues();

    // DATAJPA-1307
    @Query(value = "select u.* from SD_User u where u.emailAddress = ?")
    User findByEmailNativeAddressJdbcStyleParameter(String emailAddress);

    // NOTE: Not Supported
    // DATAJPA-1334
    default List<RolesAndFirstname> findByNamedQueryWithConstructorExpression() {
        return getOperations()
            .select(User.class)
            .get()
            .stream()
            .map(it -> (RolesAndFirstname) it)
            .collect(Collectors.toList());
    }


    interface RolesAndFirstname {

        String getFirstname();

        Set<AbstractRole> getRoles();
    }


    interface NameOnly {

        String getFirstname();

        String getLastname();
    }

    interface EmailOnly {

        String getEmailAddress();
    }
}