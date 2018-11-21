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

package requery.demo.domain;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import javax.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.util.Objects;

/**
 * City 정보를 나타내는 Requery Entity
 *
 * @author debop
 */
@Getter
@Entity(copyable = true)
public class AbstractCity extends AbstractLifecycleEntity {

    private static final long serialVersionUID = 6441380830729259194L;

    public static City of(@NotNull final String name, @Nonnull final String country) {
        City city = new City();
        city.setName(name);
        city.setCountry(country);
        return city;
    }

    @Key
    @Generated
    protected Long id;

    @Column(nullable = false)
    protected String name;

    protected String state;

    protected String country;

    protected String map;

    @Override
    public int hashCode() {
        return Objects.hash(name, state, country);
    }

    @Override
    protected  ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("state", state)
            .add("country", country);
    }
}
