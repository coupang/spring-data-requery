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

package org.springframework.data.requery.repository.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InspectionClassLoaderTest
 *
 * @author debop
 * @since 18. 6. 14
 */
public class InspectionClassLoaderTest {

    @Test
    public void shouldLoadExternalClass() throws ClassNotFoundException {

        InspectionClassLoader classLoader = new InspectionClassLoader(getClass().getClassLoader());

        Class<?> isolated = classLoader.loadClass("org.h2.Driver");
        Class<?> included = getClass().getClassLoader().loadClass("org.h2.Driver");

        assertThat(isolated.getClassLoader())
            .isSameAs(classLoader)
            .isNotSameAs(getClass().getClassLoader());

        assertThat(isolated).isNotEqualTo(included);
    }
}
