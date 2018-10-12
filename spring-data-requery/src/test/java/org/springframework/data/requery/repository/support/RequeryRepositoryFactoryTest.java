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

package org.springframework.data.requery.repository.support;

import io.requery.meta.EntityModel;
import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.aop.framework.Advised;
import org.springframework.core.OverridingClassLoader;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.data.requery.domain.basic.BasicUser;
import org.springframework.data.requery.repository.RequeryRepository;
import org.springframework.data.requery.repository.custom.CustomGenericRequeryRepositoryFactory;
import org.springframework.data.requery.repository.custom.UserCustomExtendedRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.data.repository.query.QueryLookupStrategy.Key;


@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class RequeryRepositoryFactoryTest {

    RequeryRepositoryFactory factory;

    @Mock RequeryOperations requeryOperations;
    @Mock EntityDataStore<Object> entityDataStore;
    @Mock EntityModel entityModel;
    @Mock @SuppressWarnings("rawtypes") RequeryEntityInformation entityInformation;

    @Before
    public void setup() {
        when(requeryOperations.getDataStore()).thenReturn(entityDataStore);

        factory = new RequeryRepositoryFactory(requeryOperations) {
            @SuppressWarnings("unchecked")
            @Override
            public <T, ID> RequeryEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
                return (RequeryEntityInformation<T, ID>) entityInformation;
            }
        };
        factory.setQueryLookupStrategyKey(Key.CREATE_IF_NOT_FOUND);
    }

    @Test
    public void setupBasicInstanceCorrectly() {
        assertThat(factory.getRepository(SimpleSampleRepository.class)).isNotNull();
    }

    @Test
    public void allowsCallingOfObjectMethods() {

        SimpleSampleRepository repository = factory.getRepository(SimpleSampleRepository.class);

        assertThat(repository.hashCode()).isNotEqualTo(0);
        assertThat(repository.toString()).isNotEmpty();
        assertThat(Objects.equals(repository, repository)).isTrue();
    }

    @Ignore("spring-data-jpa 에서는 NamedQuery를 이용해서 수행하는데, 굳이 그럴 필요가 있을까 싶다.")
    @Test
    public void capturesMissingCustomImplementationAndProvidesInterfacename() {
        try {
            factory.getRepository(SampleRepository.class);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains(SampleRepository.class.getName());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlesRuntimeExceptionsCorrectly() {

        RepositoryFragments fragments = RepositoryFragments.just(new SampleCustomRepositoryImpl());
        SampleRepository repository = factory.getRepository(SampleRepository.class, fragments);
        repository.throwingRuntimeException();
    }


    @Test(expected = IOException.class)
    public void handlesCheckedExceptionsCorrectly() throws Exception {

        RepositoryFragments fragments = RepositoryFragments.just(new SampleCustomRepositoryImpl());
        SampleRepository repository = factory.getRepository(SampleRepository.class, fragments);
        repository.throwingCheckedException();
    }

    @Test
    public void runDefaultMethods() {

        RepositoryFragments fragments = RepositoryFragments.just(new SampleCustomRepositoryImpl());
        SampleRepository repository = factory.getRepository(SampleRepository.class, fragments);

        BasicUser user = repository.findByEmail("debop@example.com");
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("debop@example.com");
    }

    @Test
    public void runDefaultMethodWithCustomName() {
        RepositoryFragments fragments = RepositoryFragments.just(new SampleCustomRepositoryImpl());
        SampleRepository repository = factory.getRepository(SampleRepository.class, fragments);

        BasicUser user = repository.customMethod(1L);
        assertThat(user).isNotNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createsProxyWithCustomBaseClass() {

        RequeryRepositoryFactory factory = new CustomGenericRequeryRepositoryFactory(requeryOperations);
        factory.setQueryLookupStrategyKey(Key.CREATE_IF_NOT_FOUND);
        UserCustomExtendedRepository repository = factory.getRepository(UserCustomExtendedRepository.class);

        repository.customMethod(1L);
    }

    @Ignore("spring-data-jpa 에서는 NamedQuery를 이용해서 수행하는데, 굳이 그럴 필요가 있을까 싶다.")
    @Test
    public void usesConfiguredRepositoryBaseClass() {

        factory.setRepositoryBaseClass(CustomRequeryRepository.class);

        SampleRepository repository = factory.getRepository(SampleRepository.class);
        assertThat(((Advised) repository).getTargetClass()).isEqualTo(CustomRequeryRepository.class);
    }

    @Test
    public void crudMethodMetadataPostProcessorUsesBeanClassLoader() {

        ClassLoader classLoader = new OverridingClassLoader(ClassUtils.getDefaultClassLoader());

        factory.setBeanClassLoader(classLoader);

        Object processor = ReflectionTestUtils.getField(factory, "crudMethodMetadataPostProcessor");
        Assert.notNull(processor, "processor must not be null");
        assertThat(ReflectionTestUtils.getField(processor, "classLoader")).isEqualTo(classLoader);
    }


    private interface SimpleSampleRepository extends RequeryRepository<BasicUser, Long> {

        @Transactional
        Optional<BasicUser> findById(Long id);
    }

    public interface SampleCustomRepository {

        void throwingRuntimeException();

        void throwingCheckedException() throws IOException;
    }

    private class SampleCustomRepositoryImpl implements SampleCustomRepository {

        @Override
        public void throwingRuntimeException() {
            throw new IllegalArgumentException("You lose!");
        }

        @Override
        public void throwingCheckedException() throws IOException {
            throw new IOException("You lose!");
        }
    }

    private interface SampleRepository extends RequeryRepository<BasicUser, Long>, SampleCustomRepository {

        default BasicUser findByEmail(String email) {
            BasicUser user = new BasicUser();
            user.setEmail(email);
            return user;
        }

        default BasicUser customMethod(Long id) {
            return new BasicUser();
        }
    }

    static class CustomRequeryRepository<T, ID> extends SimpleRequeryRepository<T, ID> {

        public CustomRequeryRepository(@NotNull RequeryEntityInformation<T, ID> entityInformation,
                                       @NotNull RequeryOperations operations) {
            super(entityInformation, operations);
        }
    }

}
