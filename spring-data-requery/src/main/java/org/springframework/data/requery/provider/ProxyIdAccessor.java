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

package org.springframework.data.requery.provider;

import javax.annotation.Nullable;

/**
 * Entity의 Identifier 속성에 접근할 수 있는 Accessor
 *
 * @author debop
 * @since 18. 6. 7
 */
public interface ProxyIdAccessor {

    /**
     * Accessor를 해당 entity에 사용 가능 여부
     *
     * @param entity entity to check which can access
     * @return accessable
     */
    boolean shouldUseAccessorFor(@Nullable final Object entity);

    /**
     * Entity 로부터 identifier 속성 값을 가져옵니다.
     *
     * @param entity requery entity to get identifier
     * @return identifier of requery entity
     */
    @Nullable
    Object getIdentifierFrom(@Nullable final Object entity);
}
