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

package org.springframework.data.requery.kotlin.domain.hierarchy.tree

import io.requery.Column
import io.requery.Embedded
import io.requery.Persistable
import java.io.Serializable

/**
 * org.springframework.data.requery.kotlin.domain.hierarchy.tree.NodePosition
 *
 * @author debop
 */
@Embedded
interface NodePosition : Persistable, Serializable {

    @get:Column
    var nodeLevel: Int

    @get:Column
    var nodeOrder: Int

}