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

package org.springframework.data.requery.domain.blob;


import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.Lazy;
import io.requery.Transient;
import lombok.Getter;
import org.springframework.data.requery.converters.ByteArrayToBlobConverter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * org.springframework.data.requery.domain.blob.AbstractBinaryData
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
public abstract class AbstractBinaryData extends AbstractPersistable<Long> {

    private static final long serialVersionUID = -3776835149818493670L;

    @Key
    @Generated
    protected Long id;

    @Column(nullable = false)
    protected String name;

    @Lazy
    @Convert(ByteArrayToBlobConverter.class)
    protected byte[] picture;

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Transient
    @Override
    @Nonnull
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name);
    }
}
