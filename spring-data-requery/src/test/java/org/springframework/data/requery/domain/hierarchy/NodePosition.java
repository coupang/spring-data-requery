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

package org.springframework.data.requery.domain.hierarchy;

import io.requery.Embedded;
import io.requery.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.requery.domain.AbstractValueObject;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Objects;

/**
 * NodePosition
 *
 * @author debop
 * @since 18. 6. 5
 */
@Getter
@Setter
@Embedded
public class NodePosition extends AbstractValueObject {

    private static final long serialVersionUID = -3405392108976048532L;

    protected int nodeLevel;

    protected int nodeOrder;

    @Override
    public int hashCode() {
        return Objects.hash(nodeLevel * 10000L, nodeOrder);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("nodeLevel", nodeLevel)
            .add("nodeOrder", nodeOrder);
    }
}
