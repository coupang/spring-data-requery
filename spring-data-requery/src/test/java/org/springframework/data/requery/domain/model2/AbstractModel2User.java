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
import io.requery.ReadOnly;
import io.requery.Table;
import lombok.Getter;
import org.springframework.data.requery.domain.AbstractPersistable;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * org.springframework.data.requery.domain.model2.AbstractBasicUser
 *
 * @author debop
 * @since 18. 6. 4
 */
@Getter
@Entity
@Table(name = "model2_user")
@ReadOnly
public class AbstractModel2User extends AbstractPersistable<UUID> {

    private static final long serialVersionUID = 325822952757166210L;

    @Key
    protected UUID id;

    @Key
    protected String name;

    protected Integer age;

    protected String email;

    protected Set<String> phoneNumbers;

    protected Map<String, String> attributes;

    protected URL homepage;
}
