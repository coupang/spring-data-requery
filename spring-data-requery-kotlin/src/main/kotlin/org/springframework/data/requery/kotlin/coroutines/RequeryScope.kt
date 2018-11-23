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

package org.springframework.data.requery.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Requery용 CoroutineScope
 * @author debop (Sunghyouk Bae)
 */
object RequeryScope : CoroutineScope {

    /**
     * 기본 CoroutineContext는 [Dispatchers.Default] 입니다.
     * 기본적으로 Tx 를 고려하지 않는다. 꼭 Tx를 고려해야 하는 상황에서는 Dispatchers.Main을 써야 합니다.
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}