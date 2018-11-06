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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import requery.demo.repository.CityRepository;

/**
 * DemoTestConfiguration
 *
 * @author debop
 * @since 18. 10. 31
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableRequeryRepositories(basePackageClasses = { CityRepository.class })
public class DemoTestConfiguration {


}
