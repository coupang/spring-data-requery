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

package org.springframework.data.requery.kotlin.repository.query

import org.springframework.data.repository.query.ParametersParameterAccessor

/**
 * This class provides access to parameters of a user-defined queryMethod. It wraps ParametersParameterAccessor which catches
 * special parameters Sort and Pageable.
 *
 * @author debop
 */
class RequeryParametersParameterAccessor(override val parameters: RequeryParameters,
                                         values: Array<Any>)
    : ParametersParameterAccessor(parameters, values), RequeryParameterAccessor {

    constructor(method: RequeryQueryMethod, values: Array<Any>) : this(method.parameters, values)

}