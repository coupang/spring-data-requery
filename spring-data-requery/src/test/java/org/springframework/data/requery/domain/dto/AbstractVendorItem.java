package org.springframework.data.requery.domain.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.Transient;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * AbstractVendorItem
 *
 * @author debop (Sunghyouk Bae)
 */
@Getter
@Entity(copyable = true, cacheable = false)
public class AbstractVendorItem extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -7807973626596107538L;

    @Key
    @Generated
    @Column(name = "vendorItemId")
    protected Long id;

    @Column(nullable = false)
    protected String name;

    // HINT: JSON 변환 시, bi-directional 인 경우 무한루프에 빠지는 것을 방지해준다.
    @JsonBackReference
    @ForeignKey
    @ManyToOne
    protected Vendor vendor;

    protected BigDecimal price;

    @Column(value = "false")
    protected boolean used = false;

    @Override
    public int hashCode() {
        return Objects.hash(name, used, price);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("price", price)
            .add("isUsed", used);
    }
}
