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

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link RepositoryProxyPostProcessor} that sets up interceptors to read metadata information from the invoked queryMethod.
 * This is necessary to allow redeclaration of CRUD methods in repository interfaces and configure locking information
 * or query hints on them.
 *
 * @author debop
 * @since 18. 6. 7
 */
@Slf4j
public class CrudMethodMetadataPostProcessor implements RepositoryProxyPostProcessor, BeanClassLoaderAware {

    @Nullable
    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    @Override
    public void setBeanClassLoader(@Nullable final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void postProcess(@Nonnull final ProxyFactory factory,
                            @Nonnull final RepositoryInformation repositoryInformation) {
        factory.addAdvice(CrudMethodMetadataPopulatingMethodInterceptor.INSTANCE);
    }

    /**
     * Returns a {@link CrudMethodMetadata} proxy that will lookup the actual target object by obtaining a thread bound
     * instance from the {@link TransactionSynchronizationManager} later.
     */
    public CrudMethodMetadata getCrudMethodMetadata() {

        ProxyFactory factory = new ProxyFactory();

        factory.addInterface(CrudMethodMetadata.class);
        factory.setTargetSource(new ThreadBoundTargetSource());

        return (CrudMethodMetadata) factory.getProxy(this.classLoader);
    }


    enum CrudMethodMetadataPopulatingMethodInterceptor implements MethodInterceptor {

        INSTANCE;

        private final ConcurrentHashMap<Method, CrudMethodMetadata> metadataCache = new ConcurrentHashMap<>();

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {

            Method method = invocation.getMethod();
            CrudMethodMetadata metadata = (CrudMethodMetadata) TransactionSynchronizationManager.getResource(method);

            if (metadata != null) {
                return invocation.proceed();
            }

            CrudMethodMetadata methodMetadata = metadataCache.computeIfAbsent(method, DefaultCrudMethodMetadata::new);
            TransactionSynchronizationManager.bindResource(method, methodMetadata);

            try {
                return invocation.proceed();
            } finally {
                TransactionSynchronizationManager.unbindResource(method);
            }
        }
    }

    private static class DefaultCrudMethodMetadata implements CrudMethodMetadata {
        private final Method method;

        DefaultCrudMethodMetadata(@Nonnull final Method method) {
            Assert.notNull(method, "Method must not be null!");
            this.method = method;
        }

        @Override
        public Method getMethod() {
            return this.method;
        }
    }

    private static class ThreadBoundTargetSource implements TargetSource {

        @Override
        public Class<?> getTargetClass() {
            return CrudMethodMetadata.class;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public Object getTarget() {
            MethodInvocation invocation = ExposeInvocationInterceptor.currentInvocation();
            return TransactionSynchronizationManager.getResource(invocation.getMethod());
        }

        @Override
        public void releaseTarget(@Nonnull final Object target) {
            // Nothing to do.
        }
    }
}
