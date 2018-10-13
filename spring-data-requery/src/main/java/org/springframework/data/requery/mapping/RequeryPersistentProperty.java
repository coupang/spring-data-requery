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

import org.springframework.data.mapping.PersistentProperty;

/**
 * Requery Entity의 Property로부터 여러가지 정보를 가져옵니다.
 *
 * @author debop
 * @since 18. 6. 7
 */
public interface RequeryPersistentProperty extends PersistentProperty<RequeryPersistentProperty> {

    /**
     * 현재 속성이 Identifier 속성안가?
     */
    boolean isIdProperty();

    /**
     * 현재 속성이 transient 인가?
     */
    boolean isTransient();

    /**
     * 현재 속성이 association을 나타내는 속성인가?
     */
    boolean isAssociation();

    /**
     * 현재 속성이 embedded 속성인가?
     */
    boolean isEmbedded();

    /**
     * 현 속성이 index를 가지는가?
     */
    boolean hasIndex();

    /**
     * 현 속성의 field name을 가져옵니다.
     */
    String getFieldName();

}
