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

import io.requery.CascadeAction
import io.requery.Column
import io.requery.Embedded
import io.requery.Entity
import io.requery.ForeignKey
import io.requery.Generated
import io.requery.Key
import io.requery.ManyToOne
import io.requery.OneToMany
import io.requery.Persistable
import io.requery.ReferentialAction
import java.io.Serializable

/**
 * org.springframework.data.requery.kotlin.domain.hierarchy.tree.TreeNode
 *
 * @author debop
 */
@Entity
interface TreeNode : Persistable, Serializable {

    @get:Key
    @get:Generated
    @get:Column(name = "nodeId")
    val id: Int

    var name: String

    @get:ManyToOne
    @get:ForeignKey(delete = ReferentialAction.SET_NULL, update = ReferentialAction.CASCADE)
    var parent: TreeNode?

    @get:OneToMany(mappedBy = "parent", cascade = [CascadeAction.DELETE, CascadeAction.SAVE])
    val children: MutableSet<TreeNode>

    @get:OneToMany
    val attributes: MutableSet<NodeAttribute>

    @get:Embedded
    val nodePosition: NodePosition

    //    @JvmDefault
    //    fun addChild(child: TreeNode) {
    //        child.parent = this
    //        children.add(child)
    //    }
    //
    //    @JvmDefault
    //    fun addAddribute(attr: NodeAttribute) {
    //        attr.node = this
    //        attributes.add(attr)
    //    }
}