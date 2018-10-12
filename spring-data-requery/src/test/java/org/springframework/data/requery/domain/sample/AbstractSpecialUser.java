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

import io.requery.Entity;
import io.requery.Table;

/**
 * org.springframework.data.requery.domain.sample.AbstractSpecialUser
 *
 * @author debop
 * @since 18. 6. 14
 */
// NOTE: @Superclass 가 아닌 @Entity로부터 상속받은 entity 는 잘못 될 수 있다.
@Entity
@Table(name = "SpectialUser")
public abstract class AbstractSpecialUser extends User {


    protected String specialAttribute;

    private static final long serialVersionUID = -7890331277430318674L;
}
