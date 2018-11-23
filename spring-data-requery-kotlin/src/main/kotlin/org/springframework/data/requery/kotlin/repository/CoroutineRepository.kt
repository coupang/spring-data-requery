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

package org.springframework.data.requery.kotlin.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository

/**
 * Coroutine 환경에서 실행되는 Spring Data [Repository]의 기본 Interface 입니다.
 *
 * @author debop
 * @since 18. 10. 14
 */
@NoRepositoryBean
interface CoroutineRepository<E : Any, ID : Any> : Repository<E, ID> {

    /**
     * Default [CoroutineDispatcher]
     */
    @JvmDefault
    val coroutineDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO
}