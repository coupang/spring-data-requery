package org.springframework.data.jpa.benchmark.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Diego on 2018. 10. 22..
 */
@Getter
@Setter
@Entity
public class TagValue implements Serializable {

    private static final long serialVersionUID = -968921317754306895L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long tagValueId;

    protected String rep;

    protected String synonyms;

    protected Date createdAt;

    protected Date modifiedAt;

    @ManyToOne
    protected TagType tagType;
}
