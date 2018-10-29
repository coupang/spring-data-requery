package org.springframework.data.requery.benchmark.model;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Persistable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author Diego on 2018. 10. 21..
 */
@Getter
@Setter
@Entity
public abstract class AbstractFullLog implements Persistable, Serializable {

    private static final long serialVersionUID = 4628380283053757357L;

    @Key
    @Generated
    protected Long id;

    protected Date createAt;

    protected String systemId;

    protected String systemName;

    protected Integer logLevel;

    protected String threadName;

    protected String loggerName;

    protected String logMessage;

    protected String errorMessage;

    protected String stackTrace;

    @Override
    public int hashCode() {
        return Objects.hash(createAt, systemId, logLevel, threadName);
    }

    @Override
    public String toString() {
        return "FullLog{" +
               "createAt=" + createAt +
               ", systemId='" + systemId + '\'' +
               ", systemName='" + systemName + '\'' +
               ", threadName='" + threadName + '\'' +
               ", logMessage='" + logMessage + '\'' +
               '}';
    }
}
