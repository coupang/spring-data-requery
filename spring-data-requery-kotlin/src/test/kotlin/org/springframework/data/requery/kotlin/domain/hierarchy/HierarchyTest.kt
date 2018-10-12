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

package org.springframework.data.requery.kotlin.domain.hierarchy

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.data.requery.kotlin.domain.AbstractDomainTest
import org.springframework.data.requery.kotlin.domain.hierarchy.tree.NodeAttribute
import org.springframework.data.requery.kotlin.domain.hierarchy.tree.NodeAttributeEntity
import org.springframework.data.requery.kotlin.domain.hierarchy.tree.TreeNode
import org.springframework.data.requery.kotlin.domain.hierarchy.tree.TreeNodeEntity

@Suppress("UNUSED_VARIABLE")
/**
 * org.springframework.data.requery.kotlin.domain.hierarchy.HierarchyTest
 *
 * @author debop
 */
class HierarchyTest : AbstractDomainTest() {

    companion object {
        private val log = KotlinLogging.logger { }

        fun treeNodeOf(name: String, parent: TreeNode? = null): TreeNodeEntity {

            val node = TreeNodeEntity().also {
                it.name = name
                it.nodePosition.nodeLevel = 0
                it.nodePosition.nodeLevel = 0

            }

            parent?.let {
                parent.children.add(node)
                node.parent = parent
                node.nodePosition.nodeLevel = parent.nodePosition.nodeLevel + 1
            }

            (0 until 2).forEach {
                val attr = nodeAttributeOf()
                node.attributes.add(attr)
                // attr.node = node
            }

            return node
        }

        fun nodeAttributeOf(): NodeAttributeEntity {
            return NodeAttributeEntity().apply {
                name = "name ${rnd.nextInt(1000000)}"
                value = "value ${rnd.nextInt(1000000)}"
            }
        }
    }

    @Before
    fun setup() {
        operations.deleteAll(NodeAttribute::class)
        operations.deleteAll(TreeNode::class)

        assertThat(operations.count(NodeAttribute::class).get().value()).isEqualTo(0)
        assertThat(operations.count(TreeNode::class).get().value()).isEqualTo(0)
    }

    @Test
    fun `insert root node only`() {

        val root = treeNodeOf("root")

        operations.insert(root)
        assertThat(root.id).isNotNull()

        val loadedRoot = operations.findById(TreeNode::class, root.id)!!
        assertThat(loadedRoot).isEqualTo(root)

        operations.delete(loadedRoot)

        assertThat(operations.count(NodeAttribute::class).get().value()).isEqualTo(0)
        assertThat(operations.count(TreeNode::class).get().value()).isEqualTo(0)
    }

    @Test
    fun `insert hierarchy nodes`() {

        val root = treeNodeOf("root")
        val child1 = treeNodeOf("child1", root)
        val child2 = treeNodeOf("child2", root)

        operations.insert(root)

        val loadedRoot = operations.findById(TreeNode::class, root.id)!!
        assertThat(loadedRoot).isEqualTo(root)
        assertThat(loadedRoot.children).hasSize(2).containsOnly(child1, child2)

        operations.delete(loadedRoot)

        assertThat(operations.count(NodeAttribute::class).get().value()).isEqualTo(0)
        assertThat(operations.count(TreeNode::class).get().value()).isEqualTo(0)
    }

    @Test
    fun `insert 3 generation hierarchy nodes`() {

        val root = treeNodeOf("root")
        val child1 = treeNodeOf("child1", root)
        val child2 = treeNodeOf("child2", root)

        val grandchild11 = treeNodeOf("grandChild11", child1)
        val grandchild12 = treeNodeOf("grandChild12", child1)

        val grandChild21 = treeNodeOf("grandChild21", child2)

        operations.insert(root)

        val loadedRoot = operations.findById(TreeNode::class, root.id)!!
        assertThat(loadedRoot).isEqualTo(root)
        assertThat(loadedRoot.children).hasSize(2).containsOnly(child1, child2)

        val childLoaded = operations.findById(TreeNode::class, child1.id)!!
        assertThat(childLoaded).isEqualTo(child1)
        assertThat(childLoaded.children).hasSize(2).containsOnly(grandchild11, grandchild12)

        // child1 삭제
        operations.delete(childLoaded)
        assertThat(operations.count(TreeNode::class).get().value()).isEqualTo(3)

        operations.refresh(loadedRoot)
        assertThat(loadedRoot.children).hasSize(1).contains(child2)

        operations.delete(loadedRoot)

        assertThat(operations.count(NodeAttribute::class).get().value()).isEqualTo(0)
        assertThat(operations.count(TreeNode::class).get().value()).isEqualTo(0)
    }
}