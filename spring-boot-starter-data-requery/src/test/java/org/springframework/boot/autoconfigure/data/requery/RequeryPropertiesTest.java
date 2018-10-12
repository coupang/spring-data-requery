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

package org.springframework.boot.autoconfigure.data.requery;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.autoconfigure.data.requery.domain.Models;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class RequeryPropertiesTest {

    @Test
    public void instancingEntityModel() throws Exception {

        String modelFullName = Models.class.getName() + ".DEFAULT";
        String className = StringUtils.stripFilenameExtension(modelFullName);
        String modelName = StringUtils.getFilenameExtension(modelFullName);

        assertThat(className).isNotEmpty();
        assertThat(modelName).isNotEmpty();

        Class<?> clazz = Class.forName(className);

        Field field = clazz.getField(modelName);
        field.setAccessible(true);

        Object model = field.get(null);

        log.debug("model={}", model);
        assertThat(model).isEqualTo(Models.DEFAULT);
    }
}
