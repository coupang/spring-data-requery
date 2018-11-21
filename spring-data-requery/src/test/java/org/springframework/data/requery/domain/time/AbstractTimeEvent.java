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

package org.springframework.data.requery.domain.time;

import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Key;
import io.requery.PreInsert;
import io.requery.PreUpdate;
import io.requery.Transient;
import io.requery.converter.LocalDateConverter;
import io.requery.converter.LocalDateTimeConverter;
import io.requery.converter.LocalTimeConverter;
import io.requery.converter.OffsetDateTimeConverter;
import io.requery.converter.ZonedDateTimeConverter;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Diego on 2018. 6. 12..
 */
@Getter
@Entity
public abstract class AbstractTimeEvent extends AbstractPersistable<UUID> {

    private static final long serialVersionUID = 8767142386985468901L;

    public AbstractTimeEvent() {}

    public AbstractTimeEvent(@Nonnull UUID id) {
        this.id = id;
    }

    @Key
    protected UUID id;

    protected String name;

    @Convert(LocalDateConverter.class)
    protected LocalDate localDate;

    @Convert(LocalDateTimeConverter.class)
    protected LocalDateTime localDateTime;

    @Column(name = "local_time")
    @Convert(LocalTimeConverter.class)
    protected LocalTime localTime;

    @Convert(OffsetDateTimeConverter.class)
    protected OffsetDateTime offsetDateTime;

    @Convert(ZonedDateTimeConverter.class)
    protected ZonedDateTime zonedDateTime;

    @PreInsert
    @PreUpdate
    public void onPreUpsert() {
        localDate = LocalDate.now();
        localDateTime = LocalDateTime.now();
        localTime = LocalTime.now();

        offsetDateTime = OffsetDateTime.now();
        zonedDateTime = ZonedDateTime.now();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("localDate", localDate)
            .add("localTime", localTime)
            .add("localDateTime", localDateTime)
            .add("offsetDateTime", offsetDateTime)
            .add("zonedDateTime", zonedDateTime);
    }
}
