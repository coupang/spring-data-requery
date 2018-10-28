package org.springframework.data.jpa.benchmark.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Diego on 2018. 10. 22..
 */
@Getter
@Setter
@Entity
public class TagType implements Serializable {

    private static final long serialVersionUID = 2732564348395383381L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long tagTypeId;

    protected String tagTypeClass;

    protected String tagTypeName;

    protected Date createdAt;

    protected Date modifiedAt;

    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tagType")
    protected Set<TagValue> tagValues;

    public void addTagValue(TagValue tagValue) {
        tagValue.setTagType(this);
        if (tagValues == null) {
            tagValues = new HashSet<>();
        }
        tagValues.add(tagValue);
    }
}
