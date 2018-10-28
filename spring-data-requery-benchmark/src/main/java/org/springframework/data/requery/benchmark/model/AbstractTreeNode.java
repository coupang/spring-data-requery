package org.springframework.data.requery.benchmark.model;

import io.requery.*;
import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Diego on 2018. 10. 21..
 */
@Getter
@Entity
public abstract class AbstractTreeNode implements Persistable, Serializable {

    private static final long serialVersionUID = 7051388267574303310L;

    @Key
    @Generated
    protected Long nodeId;

    protected String name;

    @ManyToOne
    @ForeignKey(referencedColumn = "nodeId", delete = ReferentialAction.SET_NULL, update = ReferentialAction.CASCADE)
    protected AbstractTreeNode parent;

    @OneToMany(mappedBy = "parent", cascade={CascadeAction.DELETE, CascadeAction.SAVE})
    protected Set<AbstractTreeNode> children;

    @OneToMany(mappedBy = "node")
    protected Set<AbstractNodeAttribute> attributes;

    protected void addChild(AbstractTreeNode child) {
        child.parent = this;
        children.add(child);
    }

    protected void addAttribute(AbstractNodeAttribute attr) {
        attr.node = this;
        attributes.add(attr);
    }
}
