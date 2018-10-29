/*
 * Copyright 2018 Coupang Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.jpa.benchmark.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.Objects;

/**
 * FullLog
 *
 * @author debop
 */
@Getter
@Setter
@Entity
public class FullLog {

    private static final long serialVersionUID = -415197041952755379L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
