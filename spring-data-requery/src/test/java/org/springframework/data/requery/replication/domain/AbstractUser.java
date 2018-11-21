package org.springframework.data.requery.replication.domain;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Table;
import io.requery.Transient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Objects;

@Getter
@Entity(model = "replications")
@Table(name = "users")
public class AbstractUser extends AbstractPersistable<Integer> {

    private static final long serialVersionUID = 1323131629156162730L;

    @Key
    @Generated
    Integer id;

    @Column(length = 50)
    String name;

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Transient
    @Override
    protected @NotNull ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name);
    }

}
