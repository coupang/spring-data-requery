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
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * org.springframework.boot.autoconfigure.data.requery.RequeryProperties
 *
 * @author debop
 */
@ConfigurationProperties("spring.data.requery")
public class RequeryProperties {

    private String modelName = "";

    private Integer batchUpdateSize = 100;

    private Integer statementCacheSize = 1024;

    private TableCreationMode tableCreationMode = TableCreationMode.CREATE_NOT_EXISTS;


    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getBatchUpdateSize() {
        return batchUpdateSize;
    }

    public void setBatchUpdateSize(Integer batchUpdateSize) {
        this.batchUpdateSize = batchUpdateSize;
    }

    public Integer getStatementCacheSize() {
        return statementCacheSize;
    }

    public void setStatementCacheSize(Integer statementCacheSize) {
        this.statementCacheSize = statementCacheSize;
    }

    public TableCreationMode getTableCreationMode() {
        return tableCreationMode;
    }

    public void setTableCreationMode(TableCreationMode tableCreationMode) {
        this.tableCreationMode = tableCreationMode;
    }

}
