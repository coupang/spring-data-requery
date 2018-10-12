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

package org.springframework.data.requery.annotation;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * org.springframework.data.requery.annotation.Query
 *
 * @author debop
 * @since 18. 6. 6
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@QueryAnnotation
@Documented
public @interface Query {

    /**
     * Defines the SQL query to be executed when the annotated queryMethod is called.
     */
    String value() default "";

    /**
     * Defines a special count query that shall be used for pagination queries to lookup the total number of elements for
     * a page. If non is configured we will derive the count query from the queryMethod name.
     */
    String countQuery() default "";

}
