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

package org.springframework.data.requery.repository.query;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.query.Param;
import org.springframework.data.requery.annotation.Query;
import org.springframework.data.requery.domain.sample.User;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.data.requery.repository.sample.UserRepository;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * org.springframework.data.requery.repository.query.RequeryQueryMethodTest
 *
 * @author debop
 * @since 18. 6. 14
 */
@RunWith(MockitoJUnitRunner.class)
public class RequeryQueryMethodTest {

    static final Class<?> DOMAIN_CLASS = User.class;
    static final String METHOD_NAME = "findByFirstname";

    @Mock RepositoryMetadata metadata;

    ProjectionFactory factory = new SpelAwareProxyProjectionFactory();

    Method invalidReturnType, pageableAndSort, pageableTwice, sortableTwice, findWithLockMethod, findsProjections,
        findsProjection, queryMethodWithCustomEntityFetchGraph;

    @Before
    public void setup() throws Exception {

        invalidReturnType = InvalidRepository.class.getMethod(METHOD_NAME, String.class, Pageable.class);
        pageableAndSort = InvalidRepository.class.getMethod(METHOD_NAME, String.class, Pageable.class, Sort.class);
        pageableTwice = InvalidRepository.class.getMethod(METHOD_NAME, String.class, Pageable.class, Pageable.class);
        sortableTwice = InvalidRepository.class.getMethod(METHOD_NAME, String.class, Sort.class, Sort.class);

//        findWithLockMethod = ValidRepository.class.getQueryMethod("findOneLocked", Integer.class);

        findsProjections = ValidRepository.class.getMethod("findsProjections");
        findsProjection = ValidRepository.class.getMethod("findsProjection");

    }

    private RequeryQueryMethod getQueryMethod(Class<?> repositoryInterface,
                                              String methodName,
                                              Class<?>... parameterTypes) throws Exception {
        Method method = repositoryInterface.getMethod(methodName, parameterTypes);
        DefaultRepositoryMetadata repositoryMetadata = new DefaultRepositoryMetadata(repositoryInterface);

        return new RequeryQueryMethod(method, repositoryMetadata, factory);
    }

    @Test
    public void testname() throws Exception {

        RequeryQueryMethod method = getQueryMethod(UserRepository.class, "findByLastname", String.class);

        assertThat(method.getName()).isEqualTo("findByLastname");
        assertThat(method.isCollectionQuery()).isTrue();
        assertThat(method.isModifyingQuery()).isFalse();
        assertThat(method.getAnnotatedQuery()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void preventsNullRepositoryMethod() {
        new RequeryQueryMethod(null, metadata, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void preventsNullQueryExtractor() throws Exception {
        Method method = UserRepository.class.getMethod("findByLastname", String.class);
        new RequeryQueryMethod(method, metadata, factory);
    }

    @Test
    public void returnsCorrectName() throws Exception {

        RequeryQueryMethod method = getQueryMethod(UserRepository.class, "findByLastname", String.class);
        assertThat(method.getName()).isEqualTo("findByLastname");
    }

    @Test
    public void returnsQueryIfAvailable() throws Exception {

        RequeryQueryMethod method = getQueryMethod(UserRepository.class, "findByLastname", String.class);
        assertThat(method.getAnnotatedQuery()).isNull();

        method = getQueryMethod(UserRepository.class, "findByAnnotatedQuery", String.class);
        assertThat(method.getAnnotatedQuery()).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsInvalidReturntypeOnPageableFinder() {
        new RequeryQueryMethod(invalidReturnType, metadata, factory);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsPageableAndSortInFinderMethod() {
        new RequeryQueryMethod(pageableAndSort, metadata, factory);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsTwoPageableParameters() {
        new RequeryQueryMethod(pageableTwice, metadata, factory);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsTwoSortableParameters() {
        new RequeryQueryMethod(sortableTwice, metadata, factory);
    }


    @Test(expected = IllegalArgumentException.class)
    public void rejectsModifyingMethodWithPageable() throws Exception {

        Method method = InvalidRepository.class.getMethod("updateMethod", String.class, Pageable.class);
        new RequeryQueryMethod(method, metadata, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsModifyingMethodWithSort() throws Exception {
        Method method = InvalidRepository.class.getMethod("updateMethod", String.class, Sort.class);
        new RequeryQueryMethod(method, metadata, factory);
    }

    @Test
    public void calculatesNamedQueryNamesCorrectly() throws Exception {

        RepositoryMetadata metadata = new DefaultRepositoryMetadata(UserRepository.class);

        RequeryQueryMethod queryMethod = getQueryMethod(UserRepository.class, "findByLastname", String.class);
        assertThat(queryMethod.getNamedQueryName()).isEqualTo("User.findByLastname");

        Method method = UserRepository.class.getMethod("renameAllUsersTo", String.class);
        assertThat(method).isNotNull();
        queryMethod = new RequeryQueryMethod(method, metadata, factory);
        assertThat(queryMethod.getNamedQueryName()).isEqualTo("User.renameAllUsersTo");

        method = UserRepository.class.getMethod("findSpecialUsersByLastname", String.class);
        assertThat(method).isNotNull();
        queryMethod = new RequeryQueryMethod(method, metadata, factory);
        assertThat(queryMethod.getNamedQueryName()).isEqualTo("SpecialUser.findSpecialUsersByLastname");
    }

    @Test
    public void rejectsInvalidNamedParameter() throws Exception {

        try {
            getQueryMethod(InvalidRepository.class, "findByAnnotatedQuery", String.class);
            fail("예외가 발생해야 합니다");
        } catch (IllegalStateException e) {
            // Parameter from query
            assertThat(e.getMessage()).contains("foo");
            // Parameter name from annotation
            assertThat(e.getMessage()).contains("param");
            // Method name
            assertThat(e.getMessage()).contains("findByAnnotatedQuery");
        }
    }

    @Test
    public void returnsTrueIfReturnTypeIsEntity() {

        when(metadata.getDomainType()).thenReturn((Class) User.class);
        when(metadata.getReturnedDomainClass(findsProjections)).thenReturn((Class) Integer.class);
        when(metadata.getReturnedDomainClass(findsProjection)).thenReturn((Class) Integer.class);

        assertThat(new RequeryQueryMethod(findsProjections, metadata, factory).isQueryForEntity()).isFalse();
        assertThat(new RequeryQueryMethod(findsProjection, metadata, factory).isQueryForEntity()).isFalse();
    }

    static interface InvalidRepository extends Repository<User, Integer> {

        // Invalid return type
        User findByFirstname(String firstname, Pageable pageable);

        // Should not use Pageable *and* Sort
        Page<User> findByFirstname(String firstname, Pageable pageable, Sort sort);

        // Must not use two Pageables
        Page<User> findByFirstname(String firstname, Pageable first, Pageable second);

        // Must not use two Sorts
        Page<User> findByFirstname(String firstname, Sort first, Sort second);

        // Not backed by a named query or @Query annotation
        // @Modifying
        void updateMethod(String firstname);

        // Modifying and Pageable is not allowed
        // @Modifying
        Page<String> updateMethod(String firstname, Pageable pageable);

        // Modifying and Sort is not allowed
        void updateMethod(String firstname, Sort sort);

        // Typo in named parameter
        @Query("select * from SD_User u where u.firstname = :foo")
        List<User> findByAnnotatedQuery(@Param("param") String param);
    }

    static interface ValidRepository extends Repository<User, Integer> {

        @Query(value = "query")
        List<User> findByLastname(String lastname);

        @Query("select * from SD_User u where u.id= ?1")
        List<User> findOne(Integer primaryKey);

        List<Integer> findsProjections();

        Integer findsProjection();

//        @CustomAnnotation
//        void withMetaAnnotation();


        @Query("select * from SD_User u where u.firstname = ?1")
        User queryWithPositionalBinding(@Param("firstname") String firstname);

    }

    static interface RequeryRepositoryOverride extends RequeryRepository<User, Integer> {

        @NotNull
        List<User> findAll();

        Optional<User> findOne(Integer id);

        User getOneById(Integer id);
    }
}
