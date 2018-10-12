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

package org.springframework.data.requery.domain.model2;

import io.requery.Entity;
import io.requery.Key;
import io.requery.Table;
import io.requery.Transient;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;


/**
 * org.springframework.data.requery.domain.model2.AbstractModel2Event
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Setter
@Entity
@Table(name = "model2_event")
public class AbstractModel2Event extends AbstractPersistable<UUID> {

    private static final long serialVersionUID = 3671180738904090633L;

    @Key
    protected UUID id;

    protected String name;

    protected LocalDate javaLocalDate;

    protected LocalDateTime javaLocalDateTime;

    protected LocalTime javaLocalTime;

    protected OffsetDateTime offsetDateTime;

    protected ZonedDateTime zonedDateTime;


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
