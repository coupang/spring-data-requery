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

package org.springframework.data.requery.repository;

import io.requery.query.Result;
import io.requery.query.Tuple;
import io.requery.query.element.QueryElement;
import io.requery.sql.StatementExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.sample.AbstractUser;
import org.springframework.data.requery.domain.sample.Role;
import org.springframework.data.requery.domain.sample.SpecialUser;
import org.springframework.data.requery.domain.sample.User;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.config.InfrastructureConfig;
import org.springframework.data.requery.repository.sample.RoleRepository;
import org.springframework.data.requery.repository.sample.UserRepository;
import org.springframework.data.requery.repository.sample.UserRepositoryImpl;
import org.springframework.data.requery.repository.support.RequeryRepositoryFactoryBean;
import org.springframework.data.requery.repository.support.SimpleRequeryRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.ExampleMatcher.matching;
import static org.springframework.data.requery.utils.RequeryUtils.unwrap;

/**
 * UserRepositoryTest
 *
 * @author debop
 * @since 18. 6. 28
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration
@Transactional
public class UserRepositoryTest {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @EnableRequeryRepositories(basePackageClasses = { UserRepository.class })
    static class TestConfiguration extends InfrastructureConfig {

        @Autowired ApplicationContext applicationContext;
        @Autowired RequeryOperations operations;

        @Bean
        public UserRepository userRepository() {

            RequeryRepositoryFactoryBean<UserRepository, User, Integer> factory = new RequeryRepositoryFactoryBean<>(UserRepository.class);

            factory.setOperations(operations);
            factory.setBeanFactory(applicationContext);
            factory.setRepositoryBaseClass(SimpleRequeryRepository.class);

            factory.setCustomImplementation(new UserRepositoryImpl());

            factory.afterPropertiesSet();

            return factory.getObject();
        }

        @Bean
        public RoleRepository roleRepository() {
            RequeryRepositoryFactoryBean<RoleRepository, Role, Integer> factory = new RequeryRepositoryFactoryBean<>(RoleRepository.class);

            return factory.getObject();
        }
    }

    @Autowired RequeryOperations operations;

    // CUT
    @Autowired UserRepository repository;

    // Test fixture
    User firstUser, secondUser, thirdUser, fourthUser;
    Integer id;
    Role adminRole;

    private static User createUser() {
        return createUser(null, null, null);
    }

    private static User createUser(String firstname, String lastname, String email, Role... roles) {
        User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmailAddress(email);
        user.setActive(true);
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        user.getRoles().addAll(Arrays.asList(roles));

        return user;
    }


    @Before
    public void setup() throws Exception {

        firstUser = createUser("Debop", "Bae", "debop@example.com");
        firstUser.setAge(51);

        secondUser = createUser("Diego", "Ahn", "diego@example.com");
        secondUser.setAge(30);

        // Before / After test 를 위해 
        Thread.sleep(10);

        thirdUser = createUser("Jinie", "Park", "jinie@example.com");
        thirdUser.setAge(26);

        fourthUser = createUser("Nickoon", "Jeon", "nickoon@example.com");
        fourthUser.setAge(35);

        adminRole = new Role("admin");

        repository.deleteAll();
    }

    @Test
    public void testCreation() {

        Integer before = operations.count(User.class).get().value();

        flushTestUsers();

        assertThat(operations.count(User.class).get().value()).isEqualTo(before + 4);
    }

    @Test
    public void testRead() {

        flushTestUsers();

        assertThat(repository.findById(id)).map(User::getFirstname).contains(firstUser.getFirstname());
    }

    @Test
    public void findAllByGivenIds() {
        flushTestUsers();
        assertThat(repository.findAllById(Arrays.asList(firstUser.getId(), secondUser.getId()))).contains(firstUser, secondUser);
    }

    @Test
    public void testReadByIdReturnsNullForNotFoundEntities() {

        flushTestUsers();
        assertThat(repository.findById(-27 * id)).isNotPresent();
    }

    @Test
    public void savesCollectionCorrectly() {
        List<User> savedUsers = repository.saveAll(Arrays.asList(firstUser, secondUser, thirdUser));

        assertThat(savedUsers).hasSize(3).containsOnly(firstUser, secondUser, thirdUser);
        savedUsers.forEach(user -> assertThat(user.getId()).isNotNull());
    }

    @Test
    public void savingEmptyCollectionIsNoOp() {

        assertThat(repository.saveAll(new ArrayList<>())).isEmpty();
    }

    @Test
    public void testUpdate() {

        flushTestUsers();

        User foundPerson = repository.findById(id).orElseThrow(() -> new IllegalStateException("Not Found"));
        foundPerson.setLastname("Kwon");

        repository.upsert(foundPerson);
        repository.refresh(foundPerson);

        assertThat(repository.findById(id)).map(User::getFirstname).contains(foundPerson.getFirstname());
    }

    @Test
    public void existReturnsWhetherAnEntityCanBeLoaded() {
        flushTestUsers();

        assertThat(repository.existsById(id)).isTrue();
        assertThat(repository.existsById(-27 * id)).isFalse();
    }

    @Test
    public void deletesAUserById() {
        flushTestUsers();

        repository.deleteById(requireNonNull(firstUser.getId()));
    }

    @Test
    public void testDelete() {
        flushTestUsers();

        repository.delete(firstUser);

        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.findById(id)).isNotPresent();
    }

    @Test
    public void returnsAllSortedCorrectly() {
        flushTestUsers();

        assertThat(repository.findAll(Sort.by(Sort.Direction.ASC, "lastname")))
            .hasSize(4)
            .containsExactly(secondUser, firstUser, fourthUser, thirdUser);
    }

    @Test
    public void deleteCollectionOfEntities() {
        flushTestUsers();

        long before = repository.count();

        repository.deleteAll(Arrays.asList(firstUser, secondUser));

        assertThat(repository.existsById(requireNonNull(firstUser.getId()))).isFalse();
        assertThat(repository.existsById(requireNonNull(secondUser.getId()))).isFalse();

        assertThat(repository.count()).isEqualTo(before - 2);
    }

    @Test
    public void batchDeleteCollectionOfEntities() {
        flushTestUsers();

        long before = repository.count();

        repository.deleteInBatch(Arrays.asList(firstUser, secondUser));

        assertThat(repository.existsById(requireNonNull(firstUser.getId()))).isFalse();
        assertThat(repository.existsById(requireNonNull(secondUser.getId()))).isFalse();

        assertThat(repository.count()).isEqualTo(before - 2);
    }

    @Test
    public void deleteEmptyCollectionDoesNotDeleteAnything() {
        assertDeleteCallDoesNotDeleteAnything(new ArrayList<>());
    }

    @Test
    public void executesManipulatingQuery() {
        flushTestUsers();

        repository.renameAllUsersTo("newLastname");

        long expected = repository.count();
        assertThat(repository.findByLastname("newLastname")).hasSize((int) expected);
    }

    @Test
    public void testFinderInvocationWithNullParameter() {

        flushTestUsers();

        repository.findByLastname(null);
    }

    @Test
    public void testFindByLastname() {
        flushTestUsers();
        assertThat(repository.findByLastname("Bae")).containsOnly(firstUser);
    }

    @Test
    public void testFindByEmailAddress() {
        flushTestUsers();
        assertThat(repository.findByEmailAddress("debop@example.com")).isEqualTo(firstUser);
    }

    @Test
    public void testReadAll() {
        flushTestUsers();

        assertThat(repository.count()).isEqualTo(4L);
        assertThat(repository.findAll()).containsOnly(firstUser, secondUser, thirdUser, fourthUser);
    }

    @Test
    public void deleteAll() {
        flushTestUsers();

        repository.deleteAll();
        assertThat(repository.count()).isZero();
    }

    @Test
    public void deleteAllInBatch() {
        flushTestUsers();

        repository.deleteAllInBatch();
        assertThat(repository.count()).isZero();
    }

    @Test
    public void testCascadesPersisting() {

        // Create link prior to persisting
        firstUser.getColleagues().add(secondUser);

        flushTestUsers();

        User firstReferenceUser = repository.findById(requireNonNull(firstUser.getId())).get();
        assertThat(firstReferenceUser).isEqualTo(firstUser);

        Set<AbstractUser> colleagues = firstReferenceUser.getColleagues();
        assertThat(colleagues).containsOnly(secondUser);
    }

    @Test
    public void testPreventsCascadingRolePersisting() {

        firstUser.getRoles().add(new Role("USER"));
        flushTestUsers();
    }

    @Test
    public void testUpsertCascadesCollegues() {

        firstUser.getColleagues().add(secondUser);
        flushTestUsers();

        firstUser.getColleagues().add(createUser("Tao", "Kim", "tagkim@example.com"));
        firstUser = repository.upsert(firstUser);

        User reference = repository.findById(requireNonNull(firstUser.getId())).get();
        Set<AbstractUser> colleagues = reference.getColleagues();

        assertThat(colleagues).hasSize(2).contains(secondUser);
    }

    @Test
    public void testCountsCorrectly() {

        long count = repository.count();

        User user = createUser("Jane", "Doe", "janedoe@example.com");
        repository.save(user);

        assertThat(repository.count()).isEqualTo(count + 1);
    }

    @Test
    public void testInvocationOfCustomImplementation() {

        repository.someCustomMethod(new User());
    }

    @Test
    public void testOverwritingFinder() {

        repository.findByOverrridingMethod();
    }

    @Test
    public void testUsesQueryAnnotation() {

        assertThat(repository.findByAnnotatedQuery("debop@example.com")).isNull();

//        flushTestUsers();
//
//        assertThat(repository.findByAnnotatedQuery("debop@example.com")).isEqualTo(firstUser);
    }

    @Test
    public void testExecutionOfProjectingMethod() {
        flushTestUsers();
        assertThat(repository.countWithFirstname("Debop")).isEqualTo(1L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executesSpecificationCorrectly() {
        flushTestUsers();
        QueryElement<? extends Result<User>> query = (QueryElement<? extends Result<User>>)
            unwrap(repository.getOperations().select(User.class).where(User.FIRSTNAME.eq("Debop")));

        assertThat(repository.findAll(query)).hasSize(1).containsOnly(firstUser);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executesSingleEntitySpecificationCorrectly() {
        flushTestUsers();
        QueryElement<? extends Result<User>> query = (QueryElement<? extends Result<User>>)
            unwrap(repository.getOperations().select(User.class).where(User.FIRSTNAME.eq("Debop")));

        assertThat(repository.findOne(query)).isPresent();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IncorrectResultSizeDataAccessException.class)
    public void throwsExceptionForUnderSpecifiedSingleEntitySpecification() {
        flushTestUsers();
        QueryElement<? extends Result<User>> query = (QueryElement<? extends Result<User>>)
            unwrap(repository.getOperations().select(User.class).where(User.FIRSTNAME.like("%e%")));

        // 2개 이상이 나온다
        repository.findOne(query);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executesCombinedSpecificationsCorrectly() {
        flushTestUsers();

        QueryElement<? extends Result<User>> query = (QueryElement<? extends Result<User>>)
            unwrap(repository.getOperations().select(User.class)
                       .where(User.FIRSTNAME.eq("Debop"))
                       .or(User.LASTNAME.eq("Ahn")));

        assertThat(repository.findAll(query)).hasSize(2).containsOnly(firstUser, secondUser);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void executesNegatingSpecificationCorrectly() {
        flushTestUsers();

        QueryElement<? extends Result<User>> query = (QueryElement<? extends Result<User>>)
            unwrap(repository.getOperations()
                       .select(User.class)
                       .where(User.FIRSTNAME.ne("Debop"))
                       .and(User.LASTNAME.eq("Ahn")));

        assertThat(repository.findAll(query)).hasSize(1).containsOnly(secondUser);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executesCombinedSpecificationsWithPageableCorrectly() {
        flushTestUsers();

        QueryElement<? extends Result<User>> query = (QueryElement<? extends Result<User>>)
            unwrap(repository.getOperations()
                       .select(User.class)
                       .where(User.FIRSTNAME.eq("Debop"))
                       .or(User.LASTNAME.eq("Ahn")));

        Page<User> users = repository.findAll(query, PageRequest.of(0, 1));
        assertThat(users.getSize()).isEqualTo(1);
        assertThat(users.hasPrevious()).isFalse();
        assertThat(users.getTotalElements()).isEqualTo(2L);
        assertThat(users.getNumberOfElements()).isEqualTo(1);
    }

    @Test
    public void executesMethodWithAnnotatedNamedParametersCorrectly() {

        firstUser = repository.save(firstUser);
        secondUser = repository.save(secondUser);

        assertThat(repository.findByLastnameOrFirstname("Ahn", "Debop"))
            .hasSize(2)
            .containsOnly(firstUser, secondUser);
    }

    @Test
    public void executesMethodWithNamedParametersCorrectlyOnMethodsWithQueryCreation() {

        firstUser = repository.save(firstUser);
        secondUser = repository.save(secondUser);

        assertThat(repository.findByFirstnameOrLastname("Debop", "Ahn"))
            .hasSize(2)
            .containsOnly(firstUser, secondUser);
    }

    @Test
    public void executesLikeAndOrderByCorrectly() {
        flushTestUsers();

        assertThat(repository.findByLastnameLikeOrderByFirstnameDesc("%a%"))
            .hasSize(2)
            .containsExactly(thirdUser, firstUser);
    }

    @Test
    public void executesNotLikeCorrectly() {
        flushTestUsers();

        assertThat(repository.findByLastnameNotLike("%ae%"))
            .hasSize(3)
            .containsOnly(secondUser, thirdUser, fourthUser);
    }

    @Test
    public void executesSimpleNotCorrectly() {
        flushTestUsers();

        assertThat(repository.findByLastnameNot("Bae"))
            .hasSize(3)
            .containsOnly(secondUser, thirdUser, fourthUser);
    }

    @Test
    public void returnsSameListIfNoSpecGiven() {

        flushTestUsers();
        assertSameElements(repository.findAll(), repository.findAll(operations.select(User.class)));
    }

    @Test
    public void returnsSameListIfNoSortIsGiven() {

        flushTestUsers();
        assertSameElements(repository.findAll(Sort.unsorted()), repository.findAll());
    }

    @Test
    public void returnsAllAsPageIfNoPageableIsGiven() {

        flushTestUsers();
        assertThat(repository.findAll(Pageable.unpaged())).isEqualTo(new PageImpl<>(repository.findAll()));
    }

    @Test
    public void removeObject() {

        flushTestUsers();
        long count = repository.count();

        repository.delete(firstUser);
        assertThat(repository.count()).isEqualTo(count - 1);
    }

    @Test
    public void executesPagedSpecificationsCorrectly() {

        Page<User> result = executeSpecWithSort(Sort.unsorted());
        assertThat(result.getContent()).isSubsetOf(firstUser, thirdUser);
    }

    @Test
    public void executesPagedSpecificationsWithSortCorrectly() {

        Page<User> result = executeSpecWithSort(Sort.by(Sort.Direction.ASC, "lastname"));
        assertThat(result.getContent()).contains(firstUser).doesNotContain(secondUser, thirdUser);
    }

    // NOTE: Not Supported
    @Test(expected = AssertionError.class)
    public void executesQueryMethodWithDeepTraversalCorrectly() {

        flushTestUsers();

        firstUser.setManager(secondUser);
        thirdUser.setManager(firstUser);
        repository.saveAll(Arrays.asList(firstUser, thirdUser));

        assertThat(repository.findByManagerLastname("Ahn")).containsOnly(firstUser);
        assertThat(repository.findByManagerLastname("Bae")).containsOnly(thirdUser);
    }

    // NOTE: Not Supported
    @Test(expected = AssertionError.class)
    public void executesFindByColleaguesLastnameCorrectly() {

        flushTestUsers();

        firstUser.getColleagues().add(secondUser);
        thirdUser.getColleagues().add(firstUser);
        repository.saveAll(Arrays.asList(firstUser, thirdUser));

        assertThat(repository.findByColleaguesLastname(secondUser.getLastname())).containsOnly(firstUser);
        assertThat(repository.findByColleaguesLastname("Bae")).containsOnly(secondUser, thirdUser);
    }

    @Test
    public void executesFindByNotNullLastnameCorrectly() {

        flushTestUsers();

        assertThat(repository.findByLastnameNotNull()).containsOnly(firstUser, secondUser, thirdUser, fourthUser);
    }

    @Test
    public void findsSortedByLastname() {

        flushTestUsers();

        assertThat(repository.findByEmailAddressLike("%@%", Sort.by(Sort.Direction.ASC, "lastname")))
            .containsExactly(secondUser, firstUser, fourthUser, thirdUser);
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void readsPageWithGroupByClauseCorrectly() {

        flushTestUsers();

        Page<String> result = repository.findByLastnameGrouped(PageRequest.of(0, 10));

        log.debug("last names={}", result.getContent());

        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    public void executesLessThatOrEqualQueriesCorrectly() {

        flushTestUsers();

        assertThat(repository.findByAgeLessThanEqual(35)).containsOnly(secondUser, thirdUser, fourthUser);
    }

    @Test
    public void executesGreaterThatOrEqualQueriesCorrectly() {

        flushTestUsers();

        assertThat(repository.findByAgeGreaterThanEqual(35)).containsOnly(firstUser, fourthUser);
    }

    @Test
    public void executesNativeQueryCorrectly() {

        flushTestUsers();
        assertThat(repository.findNativeByLastname("Bae")).containsOnly(firstUser);
    }

    @Test
    public void executesFinderWithTrueKeywordCorrectly() {

        flushTestUsers();
        firstUser.setActive(false);
        repository.upsert(firstUser);

        assertThat(repository.findByActiveTrue()).containsOnly(secondUser, thirdUser, fourthUser);
    }

    @Test
    public void executesFinderWithFalseKeywordCorrectly() {

        flushTestUsers();
        firstUser.setActive(false);
        repository.upsert(firstUser);

        assertThat(repository.findByActiveFalse()).containsOnly(firstUser);
    }

    @Test
    public void executesAnnotatedCollectionMethodCorrectly() throws InterruptedException {

        flushTestUsers();

        firstUser.getColleagues().add(thirdUser);
        repository.save(firstUser);

        List<User> result = repository.findColleaguesFor(firstUser.getId());
        assertThat(result).containsOnly(thirdUser);
    }

    @Test
    public void executesFinderWithAfterKeywordCorrectly() {

        flushTestUsers();
        assertThat(repository.findByCreatedAtAfter(secondUser.getCreatedAt())).containsOnly(thirdUser, fourthUser);
    }

    @Test
    public void executesFinderWithBeforeKeywordCorrectly() {

        flushTestUsers();
        assertThat(repository.findByCreatedAtBefore(thirdUser.getCreatedAt())).containsOnly(firstUser, secondUser);
    }

    @Test
    public void executesFinderWithStartingWithCorrectly() {

        flushTestUsers();
        assertThat(repository.findByFirstnameStartingWith("Deb")).containsOnly(firstUser);
    }

    @Test
    public void executesFinderWithEndingWithCorrectly() {

        flushTestUsers();
        assertThat(repository.findByFirstnameEndingWith("bop")).containsOnly(firstUser);
    }

    @Test
    public void executesFinderWithContainingCorrectly() {

        flushTestUsers();
        assertThat(repository.findByFirstnameContaining("n")).containsOnly(thirdUser, fourthUser);
    }

    @Test
    public void allowsExecutingPageableMethodWithUnpagedArgument() {

        flushTestUsers();

        assertThat(repository.findByFirstname("Debop", null)).containsOnly(firstUser);

        Page<User> page = repository.findByFirstnameIn(Pageable.unpaged(), "Debop", "Diego");
        assertThat(page).isNotNull();
        assertThat(page.getNumberOfElements()).isEqualTo(2);
        assertThat(page.getContent()).containsOnly(firstUser, secondUser);

        page = repository.findAll(Pageable.unpaged());
        assertThat(page.getNumberOfElements()).isEqualTo(4);
        assertThat(page.getContent()).contains(firstUser, secondUser, thirdUser, fourthUser);
    }

    @Test
    public void executesNativeQueryForNonEntitiesCorrectly() {

        flushTestUsers();

        List<Tuple> result = repository.findOnesByNativeQuery();

        assertThat(result.size()).isEqualTo(4);
        assertThat(result.get(0).<Integer>get(0)).isEqualTo(1);
    }

    @Test
    public void handlesIterableOfIdsCorrectly() {

        flushTestUsers();

        Set<Integer> set = new HashSet<>();
        set.add(firstUser.getId());
        set.add(secondUser.getId());

        assertThat(repository.findAllById(set)).containsOnly(firstUser, secondUser);
    }

    protected void flushTestUsers() {

        operations.upsert(adminRole);

        firstUser = repository.save(firstUser);
        secondUser = repository.save(secondUser);
        thirdUser = repository.save(thirdUser);
        fourthUser = repository.save(fourthUser);

        id = firstUser.getId();

        assertThat(id).isNotNull();
        assertThat(secondUser.getId()).isNotNull();
        assertThat(thirdUser.getId()).isNotNull();
        assertThat(fourthUser.getId()).isNotNull();

        assertThat(repository.existsById(id)).isTrue();
        assertThat(repository.existsById(secondUser.getId())).isTrue();
        assertThat(repository.existsById(thirdUser.getId())).isTrue();
        assertThat(repository.existsById(fourthUser.getId())).isTrue();
    }

    private static <T> void assertSameElements(Collection<T> first, Collection<T> second) {
        assertThat(first.size()).isEqualTo(second.size());

        first.forEach(it -> assertThat(second).contains(it));
        second.forEach(it -> assertThat(first).contains(it));
    }

    private void assertDeleteCallDoesNotDeleteAnything(List<User> collection) {

        flushTestUsers();
        long count = repository.count();

        repository.deleteAll(collection);
        assertThat(repository.count()).isEqualTo(count);
    }

    @Test
    public void ordersByReferencedEntityCorrectly() {

        flushTestUsers();
        firstUser.setManager(thirdUser);
        repository.upsert(firstUser);

        Page<User> all = repository.findAll(PageRequest.of(0, 10, Sort.by("manager")));

        assertThat(all.getContent()).isNotEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void bindsSortingToOuterJoinCorrectly() {

        flushTestUsers();

        Page<User> result = repository.findAllPaged(PageRequest.of(0, 10, Sort.by("manager.lastname")));

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(4);
        assertThat(result.getContent()).isNotEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void doesNotDropNullValuesOnPagedSpecificationExecution() {

        flushTestUsers();

        // NOTE: Not support association property. (ex, manager.lastname)
        QueryElement<?> whereClause = unwrap(operations.select(User.class).where(User.LASTNAME.eq("Bae")));
        Page<User> page = repository.findAll((QueryElement<? extends Result<User>>) whereClause,
                                             PageRequest.of(0, 20, Sort.by("manager.lastname")));

        assertThat(page.getNumberOfElements()).isEqualTo(1);
        assertThat(page).containsOnly(firstUser);
    }

    @Test
    public void shouldGenerateLeftOuterJoinInfindAllWithPaginationAndSortOnNestedPropertyPath() {

        firstUser.setManager(null);
        secondUser.setManager(null);
        thirdUser.setManager(firstUser);    // manager Debop
        fourthUser.setManager(secondUser);  // manager Diego

        flushTestUsers();

        // NOTE: Not support association property. (ex, manager.lastname)
        Page<User> pages = repository.findAll(PageRequest.of(0, 4, Sort.by("manager")));

        assertThat(pages.getSize()).isEqualTo(4);
        assertThat(pages.getContent().get(0).getManager()).isNull();
        assertThat(pages.getContent().get(1).getManager()).isNull();
        assertThat(pages.getContent().get(2).getManager().getFirstname()).isEqualTo("Debop");
        assertThat(pages.getContent().get(3).getManager().getFirstname()).isEqualTo("Diego");
    }

    @Test
    public void executesManualQueryWithPositionLikeExpressionCorrectly() {

        flushTestUsers();

        List<User> result = repository.findByFirstnameLike("Ni%");
        assertThat(result).containsOnly(fourthUser);

        List<User> result2 = repository.findByFirstnameLike("De%");
        assertThat(result2).hasSize(1);
    }

    // NOTE: Not supported Named parameter
    @Test(expected = StatementExecutionException.class)
    public void executesManualQueryWithNamedLikeExpressionCorrectly() {

        flushTestUsers();

        List<User> result = repository.findByFirstnameLikeNamed("Ni%");
        assertThat(result).containsOnly(fourthUser);
    }

    @Test
    public void executesDerivedCountQueryToLong() {

        flushTestUsers();
        assertThat(repository.countByLastname("Bae")).isEqualTo(1L);
    }

    @Test
    public void executesDerivedCountQueryToInt() {

        flushTestUsers();
        assertThat(repository.countUsersByFirstname("Debop")).isEqualTo(1);
    }

    @Test
    public void executesDerivedExistsQuery() {

        flushTestUsers();

        assertThat(repository.existsByLastname("Bae")).isTrue();
        assertThat(repository.existsByLastname("Donald Trump")).isFalse();
    }

    @Test
    public void findAllReturnsEmptyIterableIfNoIdsGiven() {

        assertThat(repository.findAllById(Collections.emptySet())).isEmpty();
    }

    @Ignore("결과가 Tuple 인 경우 ReturnedType 으로 변환하는 기능이 필요하다")
    @Test
    public void executesManuallyDefinedQueryWithFieldProjection() {

        flushTestUsers();
        List<String> firstnames = repository.findFirstnamesByLastname("Bae");

        firstnames.forEach(firstname -> log.debug("firstname={}", firstname));
        assertThat(firstnames).containsOnly("Debop");
    }

    @Test
    public void looksUpEntityReference() {

        flushTestUsers();

        User result = repository.getOne(firstUser.getId());
        assertThat(result).isEqualTo(firstUser);
    }

    @Test
    public void invokesQueryWithVarargsParametersCorrectly() {

        flushTestUsers();

        Collection<User> result = repository.findByIdIn(firstUser.getId(), secondUser.getId());
        assertThat(result).containsOnly(firstUser, secondUser);
    }

    @Test
    public void shouldSupportModifyingQueryWithVarArgs() {

        flushTestUsers();

        repository.updateUserActiveState(false, firstUser.getId(), secondUser.getId(), thirdUser.getId(), fourthUser.getId());

        long expectedCount = repository.count();
        assertThat(repository.findByActiveFalse()).hasSize((int) expectedCount);
        assertThat(repository.findByActiveTrue()).isEmpty();
    }

    @Test
    public void executesFinderWithOrderClauseOnly() {

        flushTestUsers();

        assertThat(repository.findAllByOrderByLastnameAsc())
            .containsExactly(secondUser, firstUser, fourthUser, thirdUser);
    }


    // NOTE: Not Supported
    @Test
    public void sortByEmbeddedProperty() {

        thirdUser.getAddress().setCountry("South Korea");
        thirdUser.getAddress().setCity("Seoul");
        thirdUser.getAddress().setStreetName("Songpa");
        thirdUser.getAddress().setStreetNo("570");

        flushTestUsers();

        Page<User> page = repository.findAll(PageRequest.of(0, 10, Sort.by("address.streetName")));
        assertThat(page.getContent()).hasSize(4);

        // Not supported
        // assertThat(page.getContent().get(3)).isEqualTo(thirdUser);
    }

    @Test
    public void findsUserByBinaryDataReference() {

        flushTestUsers();

        Collection<User> result = repository.findByIdsCustomWithPositionalVarArgs(firstUser.getId(), secondUser.getId());

        assertThat(result).containsOnly(firstUser, secondUser);
    }

    @Test
    public void customFindByQueryWithNamedVarargsParameters() {

        flushTestUsers();

        Collection<User> result = repository.findByIdsCustomWithNamedVarArgs(firstUser.getId(), secondUser.getId());

        assertThat(result).containsOnly(firstUser, secondUser);
    }

    @Ignore("Not support derived class from entity class")
    @Test
    public void saveAndFlushShouldSupportReturningSubTypesOfRepositoryEntity() {

        SpecialUser user = new SpecialUser();
        user.setFirstname("Thomas");
        user.setEmailAddress("thomas@example.org");

        // HINT: Entity class 를 상속받은 Derived class를 부모 클래스용 Repository를 사용하면 안된다.
        SpecialUser savedUser = repository.insert(user);

        assertThat(savedUser.getFirstname()).isEqualTo(user.getFirstname());
        assertThat(savedUser.getEmailAddress()).isEqualTo(user.getEmailAddress());
    }

    @Test
    public void findAllByUntypedExampleShouldReturnSubTypesOfRepositoryEntity() {

        flushTestUsers();

        List<User> result = repository.findAll(Example.of(createUser(),
                                                          matching().withIgnorePaths("age", "createdAt", "dateOfBirth")));
        assertThat(result).hasSize(4);
    }

    @Test
    public void findAllByTypedUserExampleShouldReturnSubTypesOfRepositoryEntity() {

        flushTestUsers();

        Example<User> example = Example.of(createUser(),
                                           matching().withIgnorePaths("age", "createdAt", "dateOfBirth"));
        List<User> result = repository.findAll(example);

        assertThat(result).hasSize(4);
    }

    @Test
    public void deleteByShouldReturnListOfDeletedElementsWhenRetunTypeIsCollectionLike() {

        flushTestUsers();

        Integer deletedCount = repository.deleteByLastname(firstUser.getLastname());
        assertThat(deletedCount).isEqualTo(1);

        assertThat(repository.countByLastname(firstUser.getLastname())).isEqualTo(0L);
    }

    @Test
    public void deleteByShouldReturnNumberOfEntitiesRemovedIfReturnTypeIsInteger() {

        flushTestUsers();

        Integer removedCount = repository.removeByLastname(firstUser.getLastname());
        assertThat(removedCount).isEqualTo(1);
    }


    @Test
    public void deleteByShouldReturnZeroInCaseNoEntityHasBeenRemovedAndReturnTypeIsNumber() {

        flushTestUsers();

        Integer removedCount = repository.removeByLastname("Not exists");
        assertThat(removedCount).isEqualTo(0);
    }

    @Ignore("Tuple 을 returned type 으로 추출하는 작업이 필요하다.")
    @Test
    public void findBinaryDataByIdNative() {

        byte[] data = "Woho!!".getBytes(StandardCharsets.UTF_8);
        firstUser.setBinaryData(data);

        flushTestUsers();

        // TODO: Tuple 을 returned type 으로 추출하는 작업이 필요하다.
        // TODO: @Convert 를 사용한 property 에 대해서 변환작업도 필요하다. Blob 를 byte[] 로 바꾸는 ...
        byte[] result = repository.findBinaryDataByIdNative(firstUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.length).isEqualTo(data.length);
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void findPaginatedExplicitQueryWithEmpty() {

        firstUser.setFirstname(null);

        flushTestUsers();

        assertThat(repository.count()).isEqualTo(4);

        Page<User> result = repository.findAllByFirstnameLike("Di%", PageRequest.of(0, 10));
        log.debug("search result={}", result);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    public void findPaginatedExplicitQuery() {

        flushTestUsers();

        Page<User> result = repository.findAllByFirstnameLike("De%", PageRequest.of(0, 10));
        log.debug("search result={}", result);
        log.debug("contents={}", result.getContent());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    public void findOldestUser() {

        flushTestUsers();

        User oldest = firstUser;

        assertThat(repository.findFirstByOrderByAgeDesc()).isEqualTo(oldest);
        assertThat(repository.findFirst1ByOrderByAgeDesc()).isEqualTo(oldest);
    }

    @Test
    public void findYoungestUser() {

        flushTestUsers();

        User youngest = thirdUser;

        assertThat(repository.findTopByOrderByAgeAsc()).isEqualTo(youngest);
        assertThat(repository.findTop1ByOrderByAgeAsc()).isEqualTo(youngest);
    }

    @Test
    public void find2OldestUser() {

        flushTestUsers();

        User oldest1 = firstUser;
        User oldest2 = fourthUser;

        assertThat(repository.findFirst2ByOrderByAgeDesc()).containsOnly(oldest1, oldest2);
        assertThat(repository.findTop2ByOrderByAgeDesc()).containsOnly(oldest1, oldest2);
    }

    @Test
    public void find2YoungestUser() {

        flushTestUsers();

        User youngest1 = thirdUser;
        User youngest2 = secondUser;

        assertThat(repository.findFirst2UsersBy(Sort.by("age"))).containsOnly(youngest1, youngest2);
        assertThat(repository.findTop2UsersBy(Sort.by("age"))).containsOnly(youngest1, youngest2);
    }

    @Test
    public void find3YoungestUsersPageableWithPageSize2() {

        flushTestUsers();

        User youngest1 = secondUser;
        User youngest2 = thirdUser;
        User youngest3 = fourthUser;

        Page<User> firstPage = repository.findFirst3UsersBy(PageRequest.of(0, 2, Sort.by("age")));
        assertThat(firstPage.getContent()).contains(youngest1, youngest2);

        Page<User> secondPage = repository.findFirst3UsersBy(PageRequest.of(1, 2, Sort.by("age")));
        assertThat(secondPage.getContent()).contains(youngest3);
    }

    @Test
    public void find2YoungestUsersPageableWithPageSize3() {

        flushTestUsers();

        User youngest1 = secondUser;
        User youngest2 = thirdUser;
        User youngest3 = fourthUser;

        Page<User> firstPage = repository.findFirst2UsersBy(PageRequest.of(0, 3, Sort.by("age")));
        assertThat(firstPage.getContent()).contains(youngest1, youngest2);

        Page<User> secondPage = repository.findFirst2UsersBy(PageRequest.of(1, 3, Sort.by("age")));
        assertThat(secondPage.getContent()).contains(youngest3);
    }

    @Test
    public void find3YoungestUsersPageableWithPageSize2Sliced() {

        flushTestUsers();

        User youngest1 = secondUser;
        User youngest2 = thirdUser;
        User youngest3 = fourthUser;

        Slice<User> firstPage = repository.findTop3UsersBy(PageRequest.of(0, 2, Sort.by("age")));
        assertThat(firstPage.getContent()).contains(youngest1, youngest2);

        Slice<User> secondPage = repository.findTop3UsersBy(PageRequest.of(1, 2, Sort.by("age")));
        assertThat(secondPage.getContent()).contains(youngest3);
    }

    @Test
    public void find2YoungestUsersPageableWithPageSize3Sliced() {
        flushTestUsers();

        User youngest1 = secondUser;
        User youngest2 = thirdUser;
        User youngest3 = fourthUser;

        Slice<User> firstPage = repository.findTop2UsersBy(PageRequest.of(0, 3, Sort.by("age")));
        assertThat(firstPage.getContent()).contains(youngest1, youngest2);

        Slice<User> secondPage = repository.findTop2UsersBy(PageRequest.of(1, 3, Sort.by("age")));
        assertThat(secondPage.getContent()).contains(youngest3);
    }

    @Test
    public void pageableQueryReportsTotalFromResult() {

        flushTestUsers();

        Page<User> firstPage = repository.findAll(PageRequest.of(0, 10));
        assertThat(firstPage.getContent()).hasSize(4);
        assertThat(firstPage.getTotalElements()).isEqualTo(4L);

        Page<User> secondPage = repository.findAll(PageRequest.of(1, 3));
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getTotalElements()).isEqualTo(4L);
    }

    @Test
    public void pageableQueryReportsTotalFromCount() {

        flushTestUsers();

        Page<User> firstPage = repository.findAll(PageRequest.of(0, 4));
        assertThat(firstPage.getContent()).hasSize(4);
        assertThat(firstPage.getTotalElements()).isEqualTo(4L);

        Page<User> secondPage = repository.findAll(PageRequest.of(10, 10));
        assertThat(secondPage.getContent()).hasSize(0);
        assertThat(secondPage.getTotalElements()).isEqualTo(4L);
    }

    @Test
    public void invokesQueryWithWrapperType() {

        flushTestUsers();

        // TODO: Query by property 에서는 ReturnedType 에 맞게 변형해준다. Query by Raw Query 에서 이 것을 참조해서 변경해야 한다.
        Optional<User> result = repository.findOptionalByEmailAddress("debop@example.com");

        assertThat(result.isPresent()).isEqualTo(true);
        assertThat(result.get()).isEqualTo(firstUser);
    }

    // NOTE: We will not support Spring Expression !!!


    @Test
    public void findByEmptyCollectionOfIntegers() {

        flushTestUsers();

        List<User> users = repository.findByAgeIn(Collections.emptyList());
        assertThat(users).isEmpty();
    }

    @Test
    public void findByEmptyArrayOfIntegers() {

        flushTestUsers();

        List<User> users = repository.queryByAgeIn(new Integer[0]);
        assertThat(users).isEmpty();
    }

    @Test
    public void findByAgeWithEmptyArrayOfIntegersOrFirstName() {

        flushTestUsers();

        List<User> users = repository.queryByAgeInOrFirstname(new Integer[0], secondUser.getFirstname());
        assertThat(users).hasSize(1).containsOnly(secondUser);
    }

    @Test
    public void shouldSupportJava8StreamsForRepositoryFinderMethods() {

        flushTestUsers();

        try (Stream<User> stream = repository.findAllByCustomQueryAndStream()) {
            assertThat(stream).hasSize(4);
        }
    }

    @Test
    public void shouldSupportJava8StreamsForRepositoryDerivedFinderMethods() {

        flushTestUsers();

        try (Stream<User> stream = repository.readAllByFirstnameNotNull()) {
            assertThat(stream).hasSize(4);
        }
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportsJava8StreamForPageableMethod() {

        flushTestUsers();

        try (Stream<User> stream = repository.streamAllPaged(PageRequest.of(0, 2))) {
            List<User> users = stream.collect(Collectors.toList());
            assertThat(users).hasSize(2);
        }
    }

    @Test
    public void findAllByExample() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setAge(51);
        prototype.setCreatedAt(null);

        List<User> users = repository.findAll(Example.of(prototype));

        assertThat(users).hasSize(1).containsOnly(firstUser);
    }

    @Test
    public void findAllByExampleWithEmptyProbe() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setCreatedAt(null);

        List<User> users = repository.findAll(Example.of(prototype, matching().withIgnorePaths("age", "createdAt", "active")));

        assertThat(users).hasSize(4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByNullExample() {
        repository.findAll((Example<User>) null);
    }

    @Test
    public void findAllByExampleWithExcludedAttributes() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setAge(51);
        Example<User> example = Example.of(prototype, matching().withIgnorePaths("createdAt"));

        List<User> users = repository.findAll(example);

        assertThat(users).containsOnly(firstUser);
    }

    @Test
    public void findAllByExampleWithStartingStringMatcher() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setFirstname("De");
        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withStringMatcher(ExampleMatcher.StringMatcher.STARTING)
                                               .withIgnorePaths("age", "createdAt"));

        List<User> users = repository.findAll(example);

        assertThat(users).containsOnly(firstUser);
    }

    @Test
    public void findAllByExampleWithEndingStringMatcher() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setFirstname("op");
        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withStringMatcher(ExampleMatcher.StringMatcher.ENDING)
                                               .withIgnorePaths("age", "createdAt"));

        List<User> users = repository.findAll(example);

        assertThat(users).containsOnly(firstUser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByExampleWithRegexStringMatcher() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setFirstname("^Debop$");
        Example<User> example = Example.of(prototype,
                                           matching().withStringMatcher(ExampleMatcher.StringMatcher.REGEX));

        List<User> users = repository.findAll(example);
    }

    @Test
    public void findAllByExampleWithIgnoreCase() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setFirstname("dEBoP");
        Example<User> example = Example.of(prototype,
                                           matching().withIgnoreCase().withIgnorePaths("age", "createdAt"));

        List<User> users = repository.findAll(example);
        assertThat(users).containsOnly(firstUser);
    }

    @Test
    public void findAllByExampleWithStringMatcherAndIgnoreCase() {
        flushTestUsers();

        User prototype = createUser();
        prototype.setFirstname("dEB");
        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withStringMatcher(ExampleMatcher.StringMatcher.STARTING)
                                               .withIgnoreCase()
                                               .withIgnorePaths("age", "createdAt"));

        List<User> users = repository.findAll(example);
        assertThat(users).containsOnly(firstUser);
    }

    @Test
    public void findAllByExampleWithIncludeNull() {

        flushTestUsers();

        firstUser.setDateOfBirth(new Date());

        User fifthUser = createUser(firstUser.getFirstname(), firstUser.getLastname(), "foo@bar.com");
        fifthUser.setActive(firstUser.isActive());
        fifthUser.setAge(firstUser.getAge());

        repository.saveAll(Arrays.asList(firstUser, fifthUser));

        User prototype = createUser();
        prototype.setFirstname(firstUser.getFirstname());

        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withIncludeNullValues()
                                               .withIgnorePaths("id", "binaryData", "lastname", "emailAddress", "age", "createdAt"));

        List<User> users = repository.findAll(example);
        assertThat(users).contains(fifthUser);
    }

    @Test
    public void findAllByExampleWithPropertySpecifier() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setFirstname("dEb");

        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withIgnoreCase()
                                               .withIgnorePaths("age", "createdAt")
                                               .withMatcher("firstname", new GenericPropertyMatcher().startsWith()));

        List<User> users = repository.findAll(example);
        assertThat(users).contains(firstUser);
    }

    @Test
    public void findAllByExampleWithSort() {

        flushTestUsers();

        User user1 = createUser("Debop", "Spring", "d@c.kr");
        user1.setAge(30);
        repository.save(user1);

        User prototype = createUser();
        prototype.setFirstname("dEb");

        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withIgnoreCase()
                                               .withIgnorePaths("age", "createdAt")
                                               .withStringMatcher(ExampleMatcher.StringMatcher.STARTING));

        List<User> users = repository.findAll(example, Sort.by(Sort.Direction.DESC, "age"));
        assertThat(users).contains(firstUser, user1);
    }

    @Test
    public void findAllByExampleWithPageable() {

        flushTestUsers();

        int count = 99;
        for (int i = 0; i < count; i++) {
            User user = createUser("Debop-" + i, "Spring", "debop-" + i + "@example.org");
            user.setAge(100 + i);
            repository.save(user);
        }

        User prototype = createUser();
        prototype.setFirstname("dEb");

        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withIgnoreCase()
                                               .withIgnorePaths("age", "createdAt")
                                               .withStringMatcher(ExampleMatcher.StringMatcher.STARTING));

        Page<User> users = repository.findAll(example, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "age")));

        assertThat(users.getSize()).isEqualTo(10);
        assertThat(users.hasNext()).isTrue();
        assertThat(users.getTotalElements()).isEqualTo(count + 1);
    }

    // NOTE: Association 조회를 지원하지 않기 때문에, 검증조차 하지 않습니다 ^^
    @Test // (expected = IllegalArgumentException.class)
    public void findAllByExampleShouldNotAllowCycles() {

        flushTestUsers();

        User prototype = createUser("user1", "", "");
        prototype.setManager(prototype);

        Example<User> example = Example.of(prototype,
                                           matching()
                                               .withIgnoreCase()
                                               .withIgnorePaths("age", "createdAt")
                                               .withStringMatcher(ExampleMatcher.StringMatcher.STARTING));

        repository.findAll(example);
    }

    @Test
    public void findOneByExampleWithExcludedAttributes() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setAge(firstUser.getAge());

        Example<User> example = Example.of(prototype, matching().withIgnorePaths("createdAt"));

        assertThat(repository.findOne(example)).contains(firstUser);
    }

    @Test
    public void countByExampleWithExcludedAttributes() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setAge(firstUser.getAge());

        Example<User> example = Example.of(prototype, matching().withIgnorePaths("createdAt"));

        long count = repository.count(example);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void existsByExampleWithExcludedAttributes() {

        flushTestUsers();

        User prototype = createUser();
        prototype.setAge(firstUser.getAge());

        Example<User> example = Example.of(prototype, matching().withIgnorePaths("createdAt"));

        boolean exists = repository.exists(example);

        assertThat(exists).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void executesPagedWhereClauseAnOrder() {

        flushTestUsers();

        QueryElement<?> whereClause = unwrap(operations
                                                 .select(User.class)
                                                 .where(User.LASTNAME.like("%e%"))
                                                 .orderBy(User.LASTNAME.asc()));

        Page<User> result = repository.findAll((QueryElement<? extends Result<User>>) whereClause, PageRequest.of(0, 1));

        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(firstUser);
    }

    // TODO: 수형 검사를 해야 하는데 Raw String 에서는 수행하지 않는다. (requery에서 지원하지 않는다)
    @Test
    public void exceptionsDuringParameterSettingGetThrown() {

        List<User> users = repository.findByStringAge("twelve");
        assertThat(users).isEmpty();
    }

    @Test
    public void dynamicProjectionReturningStream() {

        flushTestUsers();

        assertThat(repository.findAsStreamByFirstnameLike("%De%", User.class)).hasSize(1);
    }

    @Test
    public void dynamicProjectionReturningList() {

        flushTestUsers();

        List<User> users = repository.findAsListByFirstnameLike("%De%", User.class);
        assertThat(users).hasSize(1);
    }

    @Ignore("Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.")
    @Test
    public void supportsProjectionsWithNativeQueries() {

        flushTestUsers();

        User user = repository.findAll().get(0);

        // TODO: Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.
        UserRepository.NameOnly result = repository.findByNativeQuery(user.getId());

        assertThat(result.getFirstname()).isEqualTo(user.getFirstname());
        assertThat(result.getLastname()).isEqualTo(user.getLastname());
    }

    @Ignore("Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.")
    @Test
    public void supportsProjectionsWithNativeQueriesAndCamelCaseProperty() {

        flushTestUsers();

        User user = repository.findAll().get(0);

        // TODO: Raw SQL 의 Tuple을 Custom type 변환이 가능하도록 해야 한다.
        UserRepository.EmailOnly result = repository.findEmailOnlyByNativeQuery(user.getId());

        String emailAddress = result.getEmailAddress();

        assertThat(emailAddress)
            .isEqualTo(user.getEmailAddress())
            .as("ensuring email is actually not null")
            .isNotNull();
    }

    @Test
    public void handlesColonsFollowedByIntegerInStringLiteral() {

        String firstName = "aFirstName";

        User expected = createUser(firstName, "000:1", "something@something");
        User notExpected = createUser(firstName, "000\\:1", "something@something.else");

        repository.save(expected);
        repository.save(notExpected);

        assertThat(repository.findAll()).hasSize(2);

        List<User> users = repository.queryWithIndexedParameterAndColonFollowedByIntegerInString(firstName);

        assertThat(users).extracting(User::getId).containsExactly(expected.getId());
    }

    @Test // DATAJPA-1233
    public void handlesCountQueriesWithLessParametersSingleParam() {
        repository.findAllOrderedBySpecialNameSingleParam("Debop", PageRequest.of(2, 3));
    }

    @Test // DATAJPA-1233
    public void handlesCountQueriesWithLessParametersMoreThanOne() {
        repository.findAllOrderedBySpecialNameMultipleParams("x", "Debop", PageRequest.of(2, 3));
    }

    @Test // DATAJPA-1233
    public void handlesCountQueriesWithLessParametersMoreThanOneIndexed() {
        repository.findAllOrderedBySpecialNameMultipleParamsIndexed("x", "Debop", PageRequest.of(2, 3));
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void executeNativeQueryWithPage() {

        flushTestUsers();

        Page<User> firstPage = repository.findByNativQueryWithPageable(PageRequest.of(0, 3));
        Page<User> secondPage = repository.findByNativQueryWithPageable(PageRequest.of(1, 3));

        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getNumberOfElements()).isEqualTo(3);
        assertThat(firstPage.getContent())
            .extracting(User::getFirstname)
            .containsExactly("Debop", "Diego", "Jinie");

        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.getNumberOfElements()).isEqualTo(1);
        assertThat(secondPage.getContent())
            .extracting(User::getFirstname)
            .containsExactly("Nickoon");
    }

    @Ignore("아직 List<Tuple> 을 원하는 수형으로 변환하는 기능을 제공하지 않습니다.")
    @Test
    public void executeNativeQueryWithPageWorkaround() {

        flushTestUsers();

        Page<String> firstPage = repository.findAsStringByNativeQueryWithPageable(PageRequest.of(0, 3));
        Page<String> secondPage = repository.findAsStringByNativeQueryWithPageable(PageRequest.of(1, 3));

        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getNumberOfElements()).isEqualTo(3);
        assertThat(firstPage.getContent())
            .containsExactly("Debop", "Diego", "Jinie");

        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.getNumberOfElements()).isEqualTo(1);
        assertThat(secondPage.getContent())
            .containsExactly("Nickoon");
    }

    @Test
    public void returnsNullValueInMap() {

        firstUser.setLastname(null);
        flushTestUsers();

        Tuple tuple = repository.findTupleWithNullValues();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(tuple.<String>get("firstname")).isEqualTo("Debop");
        softly.assertThat(tuple.<String>get("lastname")).isNull();

        softly.assertThat(tuple.<String>get("non-existent")).isNull();

        softly.assertAll();
    }

    @Test
    public void testFindByEmailAddressJdbcStyleParameter() {

        flushTestUsers();

        User user = repository.findByEmailNativeAddressJdbcStyleParameter("debop@example.com");
        assertThat(user).isEqualTo(firstUser);
    }

    @SuppressWarnings("unchecked")
    private Page<User> executeSpecWithSort(Sort sort) {

        flushTestUsers();

        QueryElement<? extends Result<User>> whereClause = (QueryElement<? extends Result<User>>)
            unwrap(operations
                       .select(User.class)
                       .where(User.FIRSTNAME.eq("Debop"))
                       .or(User.LASTNAME.eq("Park")));

        Page<User> result = repository.findAll(whereClause, PageRequest.of(0, 1, sort));

        assertThat(result.getTotalElements()).isEqualTo(2L);
        return result;
    }

}
