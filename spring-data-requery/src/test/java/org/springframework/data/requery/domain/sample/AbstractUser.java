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
import io.requery.Convert;
import io.requery.Embedded;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.ManyToOne;
import io.requery.Table;
import io.requery.Transient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.requery.converters.ByteArrayToBlobConverter;
import org.springframework.data.requery.domain.AbstractPersistable;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * // NOTE: @Embedded property 가 있는 Entity에 생성자를 만들지 말라. 기본생성자에서 Embedded component를 생성한다.
 *
 * @author debop
 * @since 18. 6. 14
 */
@Getter
@Entity
@Table(name = "SD_User")
public abstract class AbstractUser extends AbstractPersistable<Integer> { // implements UserRepository.RolesAndFirstname {

    private static final long serialVersionUID = -2977575626321229838L;

    @Key
    @Generated
    protected Integer id;

    protected String firstname;
    protected String lastname;
    protected int age;
    protected boolean active;

    protected Timestamp createdAt;

    @Column(nullable = false, unique = true)
    protected String emailAddress;

    @JunctionTable(name = "User_Colleagues")
    @ManyToMany
    protected Set<AbstractUser> colleagues;

    @JunctionTable
    @ManyToMany
    protected Set<AbstractRole> roles;

    @ManyToOne
    protected AbstractUser manager;

    @Embedded
    protected AbstractAddress address;

    @Convert(ByteArrayToBlobConverter.class)
    protected byte[] binaryData;

    protected Date dateOfBirth;

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, emailAddress);
    }

    @Transient
    @Override
    protected @NotNull ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("firstname", firstname)
            .add("lastname", lastname)
            .add("emailAddress", emailAddress);
    }
}
