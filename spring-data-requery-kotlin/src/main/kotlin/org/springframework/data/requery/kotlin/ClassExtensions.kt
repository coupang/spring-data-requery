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

@file:JvmName("ClassExtensions")

package org.springframework.data.requery.kotlin

import io.requery.query.NamedExpression
import mu.KotlinLogging
import org.springframework.util.LinkedMultiValueMap
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val log = KotlinLogging.logger {}


/**
 * var 로 선언된 필드 중 non null 수형에 대해 초기화 값을 지정하고자 할 때 사용합니다.
 * 특히 ```@Autowired```, ```@Inject``` val 수형에 사용하기 좋다.
 *
 * <pre>
 *   <code>
 *      @Inject val x: Repository = uninitialized()
 *   </code>
 * </pre>
 * @see lateinit
 * @see Delegates.nonNull
 */
@Suppress("UNCHECKED_CAST")
fun <T> uninitialized(): T = null as T

fun <T> T?.asOptional(): Optional<T> = Optional.ofNullable(this)


private val classFieldCache = ConcurrentHashMap<String, Field?>()
private val classMethodCache = ConcurrentHashMap<String, Method?>()

private val entityFieldCache = LinkedMultiValueMap<Class<*>, Field>()
private val entityMethodCache = LinkedMultiValueMap<Class<*>, Method>()

fun Class<*>.findField(fieldName: String): Field? {

    val cacheKey = "$name.$fieldName"

    return classFieldCache.computeIfAbsent(cacheKey) { _ ->

        var targetClass: Class<*>? = this@findField

        while(targetClass != null && !targetClass.isAnyClass) {
            try {
                val foundField = targetClass.getDeclaredField(fieldName)
                if(foundField != null)
                    return@computeIfAbsent foundField
            } catch(e: Exception) {
                // Nothing to do.
            }
            targetClass = targetClass.superclass
        }
        null
    }
}

fun Class<*>.findFields(predicate: (Field) -> Boolean): Set<Field> {

    val foundFields = mutableSetOf<Field>()
    var targetClass: Class<*>? = this

    while(targetClass != null && !targetClass.isAnyClass) {
        val fields = targetClass.declaredFields.filter { predicate(it) }.toList()
        fields.let { foundFields.addAll(it) }

        targetClass = targetClass.superclass
    }

    return foundFields
}

fun Class<*>.findFirstField(predicate: (Field) -> Boolean): Field? {

    var targetClass: Class<*>? = findRequeryEntity()

    log.trace { "Target Requery entity class. targetClass=$targetClass" }

    while(targetClass != null && !targetClass.isAnyClass /*targetClass.isRequeryEntity*/) {
        val field = targetClass.declaredFields.find { predicate(it) }
        if(field != null)
            return field

        targetClass = targetClass.superclass
    }

    return null
}

fun Class<*>.findMethod(methodName: String, vararg paramTypes: Class<*>): Method? {
    val cacheKey = "$name.$methodName.${paramTypes.joinToString()}"

    return classMethodCache.computeIfAbsent(cacheKey) {
        var targetClass: Class<*>? = this@findMethod

        while(targetClass != null && !targetClass.isAnyClass) {
            try {
                val foundMethod = targetClass.getMethod(methodName, *paramTypes)
                if(foundMethod != null)
                    return@computeIfAbsent foundMethod
            } catch(e: NoSuchMethodException) {
                // Nothing to do.
            }

            targetClass = targetClass.superclass
        }
        null
    }
}

fun Class<*>.findMethods(predicate: (Method) -> Boolean): MutableSet<Method> {

    val foundMethods = mutableSetOf<Method>()
    var targetClass: Class<*>? = this

    while(targetClass != null && !targetClass.isAnyClass) {
        val methods = targetClass.declaredMethods?.filter { predicate(it) }?.toList()
        methods?.let { foundMethods.addAll(it) }

        targetClass = targetClass.superclass
    }

    return foundMethods
}


fun Class<*>.findFirstMethod(predicate: (Method) -> Boolean): Method? {

    var targetClass: Class<*>? = findRequeryEntity()

    while(targetClass != null && !targetClass.isAnyClass /*targetClass.isRequeryEntity*/) {
        val method = targetClass.declaredMethods.find { predicate(it) }
        if(method != null)
            return method

        targetClass = targetClass.superclass
    }

    return null
}

val Class<*>.isAnyClass: Boolean
    get() = this == Any::class.java || this == Object::class.java

val Class<*>.isRequeryEntity: Boolean
    get() = declaredAnnotations.find { it.annotationClass == io.requery.Entity::class } != null

fun Class<*>.findRequeryEntity(): Class<*>? {

    val entityName = this.getRequeryEntityName()
    val found = interfaces.find { it.name == entityName }
    log.trace { "Find Requery entity in interfaces. target=$this, found=$found" }
    if(found != null) {
        return found
    }

    var current: Class<*>? = this

    while(current != null && !current.isAnyClass) {
        log.trace { "Find Requery entity. current=$current" }
        if(current.isRequeryEntity)
            return current

        current = current.superclass
    }
    return null
}

fun Class<*>.findEntityFields(): List<Field> {

    return entityFieldCache.computeIfAbsent(this) { clazz ->
        val klazz = clazz.findRequeryEntity()
        klazz?.findFields(Field::isRequeryEntityField)?.toList()
    }
}

fun Class<*>.findEntityMethods(): List<Method> {
    return entityMethodCache.computeIfAbsent(this) { clazz ->
        val klazz = clazz.findRequeryEntity()
        klazz?.findMethods { it.isRequeryEntityMethod() }?.toList()
    }
}

fun Field.isRequeryEntityField(): Boolean {
    return !this.isRequeryGeneratedField()
}

fun Method.isRequeryEntityMethod(): Boolean {
    return !this.isRequeryGeneratedMethod() && !this.isDefault
}

fun Field.isRequeryGeneratedField(): Boolean {
    return (modifiers and Modifier.STATIC) > 0 ||
           (name == "\$proxy") ||
           (name.startsWith("\$") && name.endsWith("_state"))
}

fun Method.isRequeryGeneratedMethod(): Boolean {
    return (modifiers and Modifier.STATIC) > 0 ||
           this.isVarArgs ||
           this.parameterCount > 0
}

fun AnnotatedElement.isKeyAnnoatedElement(): Boolean = isAnnotationPresent(io.requery.Key::class.java)

fun AnnotatedElement.isTransientAnnotatedElement(): Boolean = isAnnotationPresent(io.requery.Transient::class.java)

fun AnnotatedElement.isEmbeddedAnnoatedElement(): Boolean = isAnnotationPresent(io.requery.Embedded::class.java)

fun AnnotatedElement.isAssociatedAnnotatedElement(): Boolean =
    isAnnotationPresent(io.requery.OneToOne::class.java) ||
    isAnnotationPresent(io.requery.OneToMany::class.java) ||
    isAnnotationPresent(io.requery.ManyToOne::class.java) ||
    isAnnotationPresent(io.requery.ManyToMany::class.java)


private val classKeyExpressions = ConcurrentHashMap<Class<*>, NamedExpression<*>>()
var UNKNOWN_KEY_EXPRESSION: NamedExpression<*> = NamedExpression.of("Unknown", Any::class.java)

fun <V : Any> KClass<*>.getKeyExpression(): NamedExpression<V> =
    this.java.getKeyExpression()

@Suppress("UNCHECKED_CAST")
fun <V : Any> Class<*>.getKeyExpression(): NamedExpression<V> {

    return classKeyExpressions.computeIfAbsent(this) { domainClass ->

        // NOTE: Java entity 는 Field로 등록된 id 값을 반환한다.
        // NOTE: Kotlin의 경우는 getId() 메소드로부터 반환한다.
        val field = domainClass.findFirstField { it.getAnnotation(io.requery.Key::class.java) != null }

        when(field) {
            null -> {
                val method = domainClass.findFirstMethod { it.getAnnotation(io.requery.Key::class.java) != null }
                when(method) {
                    null -> {
                        log.debug { "Not found @Key property. class=${this.simpleName} " }
                        UNKNOWN_KEY_EXPRESSION
                    }
                    else -> namedExpressionOf(method.extractGetterSetter(), method.returnType)
                }
            }
            else -> namedExpressionOf(field.name, field.type)
        }
    } as NamedExpression<V>
}

private val classPropertyExpressions = ConcurrentHashMap<String, NamedExpression<*>?>()

@Suppress("UNCHECKED_CAST")
fun Class<*>.getExpression(propertyName: String): NamedExpression<*>? {

    val key = this.name + "." + propertyName
    return classPropertyExpressions.computeIfAbsent(key) { _ ->

        // NOTE: Java entity 는 Field로 등록된 id 값을 반환한다.
        // NOTE: Kotlin의 경우는 getId() 메소드로부터 반환한다.
        val field = this@getExpression.findFirstField { it.name == propertyName }
        if(field == null) {
            val method = this.findFirstMethod { it.extractGetterSetter() == propertyName }
            method?.let { NamedExpression.of(method.extractGetterSetter(), method.returnType) }
        } else {
            NamedExpression.of(field.name, field.type)
        }
    }
}

fun Class<*>.getRequeryEntityName(): String {
    return when {
        simpleName.contains("Abstract") -> name.removePrefix("Abstract")
        simpleName.contains("Entity") -> name.removeSuffix("Entity")
        else -> simpleName
    }
}

/**
 * Getter method로부터 field name을 추출합니다.
 */
fun Method.extractGetterSetter(): String {
    return when {
        name.startsWith("get") -> name.removePrefix("get").decapitalize()
        name.startsWith("set") -> name.removePrefix("set").decapitalize()
        else -> name.decapitalize()
    }
}
