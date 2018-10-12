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

package org.springframework.data.requery.domain.sample;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Key;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.requery.domain.AbstractPersistable;

/**
 * AbstractAccount
 *
 * @author debop
 * @since 18. 6. 14
 */
@Getter
@Setter
@Entity(name = "Accounts")
public class AbstractAccount extends AbstractPersistable<Long> {

    private static final long serialVersionUID = 3616603959214418196L;

    @Key
    protected Long id;


    @Column(name = "account_name")
    protected String name;


}
