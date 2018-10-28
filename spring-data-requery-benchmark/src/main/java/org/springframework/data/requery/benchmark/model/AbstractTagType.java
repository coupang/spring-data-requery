package org.springframework.data.requery.benchmark.model;

import io.requery.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author Diego on 2018. 10. 22..
 */
@Getter
@Setter
@Entity
public abstract class AbstractTagType implements Persistable, Serializable {

    private static final long serialVersionUID = 5275622457990465155L;

    @Key
    @Generated
    protected Long tagTypeId;

    protected String tagTypeClass;

    protected String tagTypeName;

    protected Date createdAt;

    protected Date modifiedAt;

    @OneToMany(mappedBy = "tagType")
    protected Set<AbstractTagValue> tagValues;

    public void addTagValue(AbstractTagValue tagValue) {
        tagValue.tagType = this;
        tagValues.add(tagValue);
    }
}
