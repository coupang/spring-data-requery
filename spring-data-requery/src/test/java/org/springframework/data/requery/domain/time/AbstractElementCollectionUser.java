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

import io.requery.Entity;
import io.requery.Key;
import io.requery.Persistable;
import io.requery.Transient;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractValueObject;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * @author Diego on 2018. 6. 12..
 */
@Getter
@Entity
public abstract class AbstractElementCollectionUser extends AbstractValueObject implements Persistable {

    @Key
    protected UUID id;

    @Key
    protected String name;

    protected Integer age;
    protected String email;

    // HINT: JPA @ElementCollection 과 유사하게 하는 방식은 없다. Set 을 문자열로 한 컬럼에 저장하는 방식이나, OneToMany 를 사용해야 한다.
    //
    protected Set<String> phoneNumbers;
    protected Map<String, String> attributes;

    protected URL homepage;

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("id", id)
            .add("name", name)
            .add("age", age)
            .add("email", email);
    }
}
