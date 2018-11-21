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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.requery.core.RequeryOperations;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Special adapter for Springs {@link org.springframework.beans.factory.FactoryBean} interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author debop
 * @since 18. 6. 6
 */
@Slf4j
public class RequeryRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    @Nullable
    private RequeryOperations operations;

    public RequeryRepositoryFactoryBean(@Nonnull final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Autowired(required = false)
    public void setOperations(@Nullable final RequeryOperations operations) {
        this.operations = operations;
    }

    @Override
    protected void setMappingContext(@Nullable final MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        Assert.state(operations != null, "RequeryOperations must not be null!");
        return createRepositoryFactory(operations);
    }

    @Nonnull
    protected RepositoryFactorySupport createRepositoryFactory(@Nonnull final RequeryOperations requeryOperations) {
        return new RequeryRepositoryFactory(requeryOperations);
    }

    @Override
    public void afterPropertiesSet() {
//        Assert.state(operations != null, "RequeryOperations must not be null!");

        log.debug("Before afterPropertiesSet ...");
        try {
            super.afterPropertiesSet();
        } catch (Exception e) {
            log.debug("Fail to setup...", e);
        }
        log.debug("After afterPropertiesSet");
    }
}
