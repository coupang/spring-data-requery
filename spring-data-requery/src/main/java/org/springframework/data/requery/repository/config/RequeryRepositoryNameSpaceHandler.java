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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.data.repository.config.RepositoryBeanDefinitionParser;

/**
 * 특정 엔티티의 RequeryRepository에 대한 name space 설정을 처리합니다.
 *
 * @author debop
 * @since 18. 6. 6
 */
@Slf4j
public class RequeryRepositoryNameSpaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        log.info("Init and regist BeanDefinitionParser for repositories.");

        RequeryRepositoryConfigurationExtension extension = new RequeryRepositoryConfigurationExtension();
        RepositoryBeanDefinitionParser definitionParser = new RepositoryBeanDefinitionParser(extension);

        registerBeanDefinitionParser("repositories", definitionParser);

        log.info("Regist BeanDefinitionParser for repositories.");
    }
}
