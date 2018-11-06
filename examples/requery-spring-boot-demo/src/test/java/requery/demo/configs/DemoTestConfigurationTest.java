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

package requery.demo.configs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import requery.demo.repository.CityRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DemoTestConfigurationTest
 *
 * @author debop
 * @since 18. 10. 31
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoTestConfiguration.class)
public class DemoTestConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CityRepository cityRepository;

    @Test
    public void contextLoading() {
        assertThat(applicationContext).isNotNull();
        assertThat(cityRepository).isNotNull();
    }
}
