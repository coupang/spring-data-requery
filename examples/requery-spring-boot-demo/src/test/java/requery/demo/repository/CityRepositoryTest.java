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

package requery.demo.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import requery.demo.AbstractRequeryDemoTest;
import requery.demo.domain.City;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CityRepositoryTest
 *
 * @author debop
 * @since 18. 11. 1
 */
@Slf4j
public class CityRepositoryTest extends AbstractRequeryDemoTest {

    @Autowired
    private CityRepository repository;

    @Test
    public void dynamicCreated() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void basicCrud() {
        repository.deleteAll();

        City seoul = City.of("Seoul", "Korea");
        repository.save(seoul);

        assertThat(seoul.getId()).isNotNull();
        assertThat(seoul.getCreatedDate()).isNotNull();

        City loaded = repository.findByIdAndDeletedFalse(seoul.getId());
        assertThat(loaded).isEqualTo(seoul);
    }

}
