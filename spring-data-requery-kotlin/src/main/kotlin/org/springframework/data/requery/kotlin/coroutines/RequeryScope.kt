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

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Requery용 CoroutineScope
 * @author debop (Sunghyouk Bae)
 */
object RequeryScope : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default // CompletableEntityStore 가 ForkJoin commonPool 을 사용한다. 이는 ResultSet에 대한 처리만 수행한다는 뜻이다.
}