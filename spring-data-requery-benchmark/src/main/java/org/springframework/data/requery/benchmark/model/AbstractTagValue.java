package org.springframework.data.requery.benchmark.model;

import io.requery.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Diego on 2018. 10. 22..
 */
@Getter
@Setter
@Entity
public abstract class AbstractTagValue implements Persistable, Serializable {

    private static final long serialVersionUID = -6228352484773269722L;

    @Key
    @Generated
    protected Long tagValueId;

    protected String rep;

    protected String synonyms;

    protected Date createdAt;

    protected Date modifiedAt;

    @ManyToOne(cascade = {CascadeAction.SAVE, CascadeAction.DELETE})
    @ForeignKey(referencedColumn = "tagTypeId", delete = ReferentialAction.SET_NULL, update = ReferentialAction.CASCADE)
    protected AbstractTagType tagType;
}
