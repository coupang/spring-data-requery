package org.springframework.data.requery.benchmark.model;

import io.requery.*;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author Diego on 2018. 10. 21..
 */
@Getter
@Entity
public abstract class AbstractNodeAttribute implements Persistable, Serializable {

    private static final long serialVersionUID = -9006338542694263309L;

    @Key
    @Generated
    protected Long id;

    protected String name;

    protected String attrValue;

    @ManyToOne(cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
    @ForeignKey(referencedColumn = "nodeId")
    protected AbstractTreeNode node;
}
