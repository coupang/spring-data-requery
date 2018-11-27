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

import io.requery.sql.TableCreationMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot AutoConfiguration 사용 시 Requery와 관련된 환경설정 값을 읽어옵니다.
 *
 * @author debop
 */
@Getter
@Setter
@ConfigurationProperties("spring.data.requery")
public class RequeryProperties {

    /**
     * Requery Model name
     */
    private String modelName = "";

    /**
     * Default batch update size
     */
    private Integer batchUpdateSize = 100;

    /**
     * Statement cache size
     */
    private Integer statementCacheSize = 1024;

    /**
     * Table creation mode
     */
    private TableCreationMode tableCreationMode; // = TableCreationMode.CREATE_NOT_EXISTS;

}
