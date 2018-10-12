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

import io.requery.query.Tuple
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.data.repository.query.Param
import org.springframework.data.requery.kotlin.findMethod
import java.time.LocalDateTime
import java.util.*

class RequeryParametersTest {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    @Test
    fun `find parameter configuration`() {

        val method = SampleRepository::class.java.findMethod("foo", LocalDateTime::class.java, String::class.java)
        assertThat(method).isNotNull

        val parameters = RequeryParameters(method!!)

        val parameter = parameters.getBindableParameter(0)
        log.debug { "parameter1 = ${parameter.name}" }
        assertThat(parameter.isSpecialParameter).isFalse()
        assertThat(parameter.isBindable).isTrue()
        assertThat(parameter.isNamedParameter).isTrue()
        assertThat(parameter.name.get()).isEqualTo("time")

        val parameter2 = parameters.getBindableParameter(1)
        log.debug { "parameter2 = ${parameter2.name}" }
        assertThat(parameter2.isSpecialParameter).isFalse()
        assertThat(parameter2.isBindable).isTrue()
        assertThat(parameter2.isNamedParameter).isTrue()
        assertThat(parameter2.name.get()).isEqualTo("firstname")
    }

    @Test
    fun `find primitive parameters`() {

        val method = SampleRepository::class.java.findMethod("bar", Int::class.java, String::class.java)
        assertThat(method!!).isNotNull

        log.debug { "return type=${method.returnType.name}" }
        log.debug { "return generic type=${method.genericReturnType.typeName}" }

        val parameters = RequeryParameters(method)

        val parameter = parameters.getBindableParameter(0)
        log.debug { "parameter1 = ${parameter.name}" }
        assertThat(parameter.isSpecialParameter).isFalse()

        // NOTE: Java interface에서는 named parameter가 false이고, Kotlin interface에서는 named parameter가 true 이다.
        assertThat(parameter.isNamedParameter).isTrue()
        assertThat(parameter.isExplicitlyNamed).isFalse()

        val parameter2 = parameters.getBindableParameter(1)
        log.debug { "parameter2 = ${parameter2.name}" }
        assertThat(parameter2.isSpecialParameter).isFalse()
        // NOTE: Java interface에서는 named parameter가 false이고, Kotlin interface에서는 named parameter가 true 이다.
        assertThat(parameter2.isNamedParameter).isTrue()
        assertThat(parameter2.isExplicitlyNamed).isFalse()
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `run default method in interface`() {

        val method = SampleRepository::class.java.findMethod("findByName", String::class.java)
        assertThat(method!!).isNotNull
        assertThat(method.isDefault).isTrue()

        // HINT: 이렇게 default method 에 대해서도 직접 실행이 가능하도록 할 수 있다.

        val repository = SampleRepositoryImpl()
        val result = method.invoke(repository, "value1") as Optional<String>
        assertThat(result.get()).isEqualTo("value1")
    }


    interface SampleRepository {

        fun foo(@Param("time") localTime: LocalDateTime, @Param("firstname") name: String)

        fun bar(age: Int, email: String): List<Tuple>?

        @JvmDefault
        fun findByName(name: String?): Optional<String> {
            return Optional.ofNullable(name)
        }
    }

    class SampleRepositoryImpl : SampleRepository {

        override fun foo(localTime: LocalDateTime, name: String) {
            // Nothing to dp
        }

        override fun bar(age: Int, email: String): List<Tuple>? {
            return null
        }
    }
}