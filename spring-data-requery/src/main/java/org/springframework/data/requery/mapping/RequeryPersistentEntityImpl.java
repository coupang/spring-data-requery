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

package org.springframework.data.requery.mapping;//package com.coupang.springframework.data.requery.mapping;
//
//import org.springframework.data.requery.provider.ProxyIdAccessor;
//import io.requery.Version;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.data.mapping.IdentifierAccessor;
//import org.springframework.data.mapping.PersistentEntity;
//import org.springframework.data.mapping.model.BasicPersistentEntity;
//import org.springframework.data.mapping.model.IdPropertyIdentifierAccessor;
//import org.springframework.data.util.TypeInformation;
//import org.springframework.util.Assert;
//
///**
// * Implementation of {@link RequeryPersistentEntity}.
// *
// * @author debop
// * @since 18. 6. 7
// */
//public class RequeryPersistentEntityImpl<T>
//    extends BasicPersistentEntity<T, RequeryPersistentProperty> implements RequeryPersistentEntity<T> {
//
//    private static final String INVALID_VERSION_ANNOTATION =
//        "%s is annotated with " + org.springframework.data.annotation.Version.class.getName() +
//        " but needs to use " + javax.persistence.Version.class.getName() + " to trigger optimistic locking correctly!";
//
//    private final ProxyIdAccessor proxyIdAccessor;
//
//    public RequeryPersistentEntityImpl(TypeInformation<T> information,
//                                       ProxyIdAccessor proxyIdAccessor) {
//        super(information, null);
//        Assert.notNull(proxyIdAccessor, "ProxyIdAccessor must not be null!");
//        this.proxyIdAccessor = proxyIdAccessor;
//    }
//
//    @Override
//    protected RequeryPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(RequeryPersistentProperty property) {
//        return property.isIdProperty() ? property : null;
//    }
//
//    @NotNull
//    @Override
//    public IdentifierAccessor getIdentifierAccessor(@NotNull Object bean) {
//        return new RequeryProxyAwareIdentifierAccessor(this, bean, proxyIdAccessor);
//    }
//
//    @Override
//    public void verify() {
//        super.verify();
//
//        RequeryPersistentProperty versionProperty = getVersionProperty();
//
//        if (versionProperty != null && versionProperty.isAnnotationPresent(Version.class)) {
//            throw new IllegalArgumentException(String.format(INVALID_VERSION_ANNOTATION, versionProperty));
//        }
//    }
//
//
//    /**
//     * {@link IdentifierAccessor} that tries to use a {@link ProxyIdAccessor} for id access to potentially avoid the
//     * initialization of JPA proxies. We're falling back to the default behavior of {@link IdPropertyIdentifierAccessor}
//     * if that's not possible.
//     */
//    private static class RequeryProxyAwareIdentifierAccessor extends IdPropertyIdentifierAccessor {
//
//        private final Object bean;
//        private final ProxyIdAccessor proxyIdAccessor;
//
//        /**
//         * Creates a new {@link IdPropertyIdentifierAccessor} for the given {@link PersistentEntity}.
//         *
//         * @param entity must not be {@literal null}.
//         */
//        public RequeryProxyAwareIdentifierAccessor(PersistentEntity<?, ?> entity, Object bean, ProxyIdAccessor proxyIdAccessor) {
//            super(entity, bean);
//            Assert.notNull(proxyIdAccessor, "ProxyIdAccessor must not be null!");
//
//            this.proxyIdAccessor = proxyIdAccessor;
//            this.bean = bean;
//        }
//
//        @Override
//        public Object getIdentifier() {
//            return proxyIdAccessor.shouldUseAccessorFor(bean)
//                   ? proxyIdAccessor.getIdentifierFrom(bean)
//                   : super.getIdentifier();
//        }
//    }
//}
