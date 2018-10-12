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

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * CommandType
 *
 * @author debop
 * @since 18. 6. 27
 */
@Slf4j
public enum CommandType {
    NONE,
    SELECT,
    INSERT,
    UPDATE,
    UPSERT,
    DELETE,
    REFRESH;


    public static CommandType parse(String methodName) {
        log.debug("Parse methodName={}", methodName);

        if (!StringUtils.hasText(methodName)) {
            return NONE;
        }
        String method = methodName.toLowerCase();

        if (method.startsWith("insert")) {
            return INSERT;
        }
        if (method.startsWith("update")) {
            return UPDATE;
        }
        if (method.startsWith("upsert")) {
            return UPSERT;
        }
        if (method.startsWith("delete") || method.startsWith("remove")) {
            return DELETE;
        }
        return SELECT;
    }
}
