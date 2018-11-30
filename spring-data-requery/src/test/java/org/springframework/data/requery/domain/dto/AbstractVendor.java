package org.springframework.data.requery.domain.dto;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToMany;
import io.requery.Transient;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * AbstractVendor
 *
 * @author debop (Sunghyouk Bae)
 */
@Getter
@Entity(copyable = true, cacheable = false)
public class AbstractVendor extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -6927282765739973055L;

    @Key
    @Generated
    @Column(name = "vendorId")
    protected Long id;

    protected String name;

    protected Date registeredAt;

    @OneToMany(mappedBy = "vendor", cascade = { CascadeAction.DELETE, CascadeAction.SAVE })
    protected Set<VendorItem> items;

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("registeredAt", registeredAt);
    }
}
