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

package org.springframework.data.requery.domain.hierarchy;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Embedded;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import io.requery.ReferentialAction;
import io.requery.Table;
import io.requery.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Objects;
import java.util.Set;

/**
 * org.springframework.data.requery.domain.hierarchy.AbstractTreeNode
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Setter
@Entity
@Table(name = "tree_node")
public class AbstractTreeNode extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -4267441735293906937L;

    @Key
    @Generated
    @Column(name = "nodeId")
    protected Long id;

    @Column(name = "nodeName", length = 48)
    protected String name;

    @ManyToOne
    @ForeignKey(delete = ReferentialAction.SET_NULL, update = ReferentialAction.CASCADE)
    protected AbstractTreeNode parent;

    @OneToMany(mappedBy = "parent", cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected Set<AbstractTreeNode> children;

    @OneToMany(mappedBy = "node")
    protected Set<NodeAttribute> attributes;

    @Embedded
    protected NodePosition nodePosition;

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name);
    }
}
