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

package org.springframework.data.requery.repository.query;

import io.requery.query.Tuple;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * RequeryResultConverter
 *
 * @author debop
 * @since 18. 6. 28
 */
@Slf4j
@UtilityClass
public class RequeryResultConverter {

    public static Object convertResult(Object result) {
        return convertResult(result, null);
    }

    public static Object convertResult(Object result, Object defaultValue) {

        log.trace("Convert result... result={}", result);
        try {
            if (result instanceof Tuple) {
                Tuple tuple = (Tuple) result;
                if (tuple.count() == 1)
                    return tuple.get(0);

                return tuple;
            }
            return result;

        } catch (Exception e) {
            log.warn("Fail to convert result[{}]. return [{}]", result, defaultValue);
            return defaultValue;
        }
    }
}
