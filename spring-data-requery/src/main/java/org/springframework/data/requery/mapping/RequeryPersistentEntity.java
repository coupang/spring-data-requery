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

package org.springframework.data.requery.mapping;

import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.PersistentEntity;

import java.util.Collection;

/**
 * Requery Entity 에서 id, index 정보등을 추출할 수 있도록 하는 {@link PersistentEntity}
 *
 * @author debop
 * @since 18. 6. 7
 */
public interface RequeryPersistentEntity<T> extends PersistentEntity<T, RequeryPersistentProperty>, ApplicationContextAware {

    /**
     * Entity의 Identifier 속성이 한 개인 경우 해당 속성에 대한 {@link RequeryPersistentProperty} 정보를 반환합니다.
     */
    RequeryPersistentProperty getSingleIdProperty();

    /**
     * Entity의 모든 identifier 에 대한 {@link RequeryPersistentProperty}의 컬렉션을 반환합니다.
     */
    Collection<RequeryPersistentProperty> getIdProperties();

    /**
     * 모든 index 정보를 반환합니다.
     */
    Collection<RequeryPersistentProperty> getIndexes();

    /**
     * embedded entity에 대한 {@link RequeryPersistentProperty}를 제공합니다.
     */
    Collection<RequeryPersistentProperty> getEmbeddedProperties();
}
