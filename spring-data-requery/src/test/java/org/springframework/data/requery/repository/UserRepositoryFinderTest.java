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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.requery.configs.RequeryTestConfiguration;
import org.springframework.data.requery.domain.sample.Role;
import org.springframework.data.requery.domain.sample.User;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.data.requery.repository.sample.RoleRepository;
import org.springframework.data.requery.repository.sample.UserRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Integration test for executing finders, thus testing various query lookup strategies.
 *
 * @author debop
 * @since 18. 6. 30
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@Transactional
public class UserRepositoryFinderTest {

    @Configuration
    @EnableRequeryRepositories(basePackageClasses = { UserRepository.class })
    static class TestConfiguration extends RequeryTestConfiguration {

    }

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    User dave, carter, oliver;
    Role drummer, guitarist, singer;


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
    public void setup() {

        drummer = roleRepository.save(new Role("DRUMMER"));
        guitarist = roleRepository.save(new Role("GUITARIST"));
        singer = roleRepository.save(new Role("SINGER"));

        dave = userRepository.save(createUser("Dave", "Matthews", "dave@dmband.com", singer));
        carter = userRepository.save(createUser("Carter", "Beauford", "carter@dmband.com", singer, drummer));
        oliver = userRepository.save(createUser("Oliver August", "Matthews", "oliver@dmband.com"));
    }

    @After
    public void clearUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    public void testSimpleCustomCreatedFinder() {

        User user = userRepository.findByEmailAddressAndLastname("dave@dmband.com", "Matthews");
        assertThat(user).isEqualTo(dave);
    }

    @Test
    public void returnsNullIfNothingFound() {

        User user = userRepository.findByEmailAddress("foobar");
        assertThat(user).isNull();
    }

    /**
     * Tests creation of a simple query consisting of {@code AND} and {@code OR} parts.
     */
    @Test
    public void testAndOrFinder() {

        List<User> users = userRepository.findByEmailAddressAndLastnameOrFirstname("dave@dmband.com",
                                                                                   "Matthews",
                                                                                   "Carter");
        assertThat(users)
            .hasSize(2)
            .containsOnly(dave, carter);
    }

    @Test
    public void executesPagingMethodToPageCorrectly() {

        List<User> list = userRepository.findByFirstname("Carter", PageRequest.of(0, 1));
        assertThat(list).hasSize(1);
    }

    @Test
    public void executeInKeywordForPageCollectly() {

        Page<User> page = userRepository.findByFirstnameIn(PageRequest.of(0, 1), "Dave", "Oliver August");

        assertThat(page.getNumberOfElements()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(2L);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    public void executesNotInQueryCorrectly() {

        List<User> result = userRepository.findByFirstnameNotIn(Arrays.asList("Dave", "Carter"));
        assertThat(result).hasSize(1).containsOnly(oliver);
    }

    @Test
    public void findsByLastnameIgnoringCase() {

        List<User> result = userRepository.findByLastnameIgnoringCase("BeAUfoRd");
        assertThat(result).hasSize(1).containsOnly(carter);
    }

    @Test
    public void findsByLastnameIgnoringCaseLike() {

        List<User> result = userRepository.findByLastnameIgnoringCaseLike("BeAUfo%");
        assertThat(result).hasSize(1).containsOnly(carter);
    }

    @Test
    public void findByLastnameAndFirstnameAllIgnoringCase() {
        List<User> result = userRepository.findByLastnameAndFirstnameAllIgnoringCase("MaTTheWs", "DaVe");
        assertThat(result).hasSize(1).containsOnly(dave);
    }

    @Test
    public void respectsPageableOrderOnQueryGenerateFromMethodName() {

        Page<User> ascending = userRepository.findByLastnameIgnoringCase(PageRequest.of(0, 10, Sort.by(ASC, "firstname")),
                                                                         "Matthews");

        Page<User> desending = userRepository.findByLastnameIgnoringCase(PageRequest.of(0, 10, Sort.by(DESC, "firstname")),
                                                                         "Matthews");
        assertThat(ascending.getTotalElements()).isEqualTo(2L);
        assertThat(desending.getTotalElements()).isEqualTo(2L);

        assertThat(ascending.getContent().get(0).getFirstname())
            .isNotEqualTo(desending.getContent().get(0).getFirstname());

        assertThat(ascending.getContent().get(1).getFirstname())
            .isEqualTo(desending.getContent().get(0).getFirstname());
    }

    @Test
    public void executesQueryToSlice() {

        Slice<User> slice = userRepository.findSliceByLastname("Matthews",
                                                               PageRequest.of(0, 1, ASC, "firstname"));

        assertThat(slice.getContent()).contains(dave);
        assertThat(slice.hasNext()).isTrue();
    }

    @Test
    public void executesMethodWithNotContainingOnStringCorrectly() {
        List<User> users = userRepository.findByLastnameNotContaining("u");
        assertThat(users).containsOnly(dave, oliver);
    }

    @Test
    public void translatesContainsToMemberOf() {

        List<User> singers = userRepository.findByRolesContaining(singer);

        assertThat(singers).hasSize(2).contains(dave, carter);

        assertThat(userRepository.findByRolesContaining(drummer)).contains(carter);
    }

    @Test
    public void translatesNotContainsToNotMemberOf() {
        assertThat(userRepository.findByRolesNotContaining(drummer)).contains(dave, oliver);
    }

    @Test // (expected = InvalidDataAccessApiUsageException.class) // DATAJPA-1023, DATACMNS-959
    public void rejectsStreamExecutionIfNoSurroundingTransactionActive() {
        userRepository.findAllByCustomQueryAndStream();
    }

    @Ignore("Not support Named query")
    @Test // DATAJPA-1334
    public void executesNamedQueryWithConstructorExpression() {
        userRepository.findByNamedQueryWithConstructorExpression();
    }
}
