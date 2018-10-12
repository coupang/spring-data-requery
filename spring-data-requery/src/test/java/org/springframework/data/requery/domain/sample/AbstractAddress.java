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

import io.requery.Embedded;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.requery.domain.AbstractComponent;

/**
 * AbstractAddress
 *
 * @author debop
 * @since 18. 6. 14
 */
@Getter
@Setter
@Embedded
public class AbstractAddress extends AbstractComponent {

    private static final long serialVersionUID = 6292061477905475831L;

    protected String country;
    protected String city;
    protected String streetName;
    protected String streetNo;

    public AbstractAddress() {}

//    public AbstractAddress(String country, String city, String streetName, String streetNo) {
//        this.country = country;
//        this.city = city;
//        this.streetName = streetName;
//        this.streetNo = streetNo;
//    }

}
