package org.springframework.boot.autoconfigure.data.requery.domain;

import io.requery.Superclass;
import lombok.Getter;
import org.springframework.data.requery.domain.AuditableLongEntity;

/**
 * AbstractLifecycleEntity
 *
 * @author debop (Sunghyouk Bae)
 */
@Getter
@Superclass
public abstract class AbstractLifecycleEntity extends AuditableLongEntity {

    private static final long serialVersionUID = -2278434123348362778L;

    protected boolean deleted;
}
