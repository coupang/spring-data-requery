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

/**
 * 수형에 지정한 필드명에 해당하는 [Field]를 찾는다.
 *
 * @param fieldName field name to find
 */
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

/**
 * 해당 조건에 맞는 Field 들을 찾습니다.
 *
 * @param predicate condition to find
 */
fun Class<*>.findFields(predicate: (Field) -> Boolean): Set<Field> {

    val foundFields = mutableSetOf<Field>()
    var targetClass: Class<*>? = this

    while(targetClass != null && !targetClass.isAnyClass) {
        val fields = targetClass.declaredFields.filter { predicate(it) }.toList()
        foundFields.addAll(fields)
        targetClass = targetClass.superclass
    }

    return foundFields
}

/**
 * 해당 조건에 맞는 Field 중 첫번 째 Field를 반환한다
 * @param predicate
 */
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

/**
 * 지정한 메소드명과 인자를 가지는 [Method]를 찾습니다.
 *
 * @param methodName method name to find
 * @param paramTypes types of method parameters
 */
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

/**
 * 지정한 조건에 맞는 모든 [Method]를 찾습니다.
 * @param predicate conditions to find
 */
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

/**
 * 해당 수형에서 Requery용 Property의 [Field] 정보들을 가져옵니다.
 */
fun Class<*>.findEntityFields(): List<Field> {
    return entityFieldCache.computeIfAbsent(this) { clazz ->
        clazz.findRequeryEntity()
            ?.findFields(Field::isRequeryEntityField)
            ?.toList()
        ?: emptyList<Field>()
    }
}

/**
 * 해당 수형에서 Requery용 Method 정보를 추출합니다.
 */
fun Class<*>.findEntityMethods(): List<Method> {
    return entityMethodCache.computeIfAbsent(this) { clazz ->
        clazz.findRequeryEntity()
            ?.findMethods(Method::isRequeryEntityMethod)
            ?.toList()
        ?: emptyList<Method>()
    }
}

/**
 * Requery용 Field 중에 requery processor가 생성한 Field를 제외한 실제 엔티티의 필드인지 검사한다
 */
fun Field.isRequeryEntityField(): Boolean {
    return !this.isRequeryGeneratedField()
}

/**
 * Requery용 Method중에 requery processor가 생성한 Method를 제외한 실제 엔티티의 필드인지 검사한다
 */
fun Method.isRequeryEntityMethod(): Boolean {
    return !this.isRequeryGeneratedMethod() && !this.isDefault
}

/**
 * 해당 Field가 requery-processor로부터 생성된 field 인지 여부
 */
fun Field.isRequeryGeneratedField(): Boolean {
    return (modifiers and Modifier.STATIC) > 0 ||
           (name == "\$proxy") ||
           (name.startsWith("\$") && name.endsWith("_state"))
}

/**
 * 해당 Method가 requery-processor로부터 생성된 method 인지 여부
 */
fun Method.isRequeryGeneratedMethod(): Boolean {
    return (modifiers and Modifier.STATIC) > 0 ||
           this.isVarArgs ||
           this.parameterCount > 0
}

/**
 * 해당 필드나 메소드가 requery의 `@Key` annotation이 있는지 여부
 */
fun AnnotatedElement.isKeyAnnoatedElement(): Boolean = isAnnotationPresent(io.requery.Key::class.java)

/**
 * 해당 필드나 메소드가 DB에 저장할 필요 없음을 나타내는 requery의 `@Transient` annotation이 있는지 여부
 */
fun AnnotatedElement.isTransientAnnotatedElement(): Boolean = isAnnotationPresent(io.requery.Transient::class.java)

/**
 * 해당 필드나 메소드가 requery의 `@Embedded` annotation이 있는지 여부
 */
fun AnnotatedElement.isEmbeddedAnnoatedElement(): Boolean = isAnnotationPresent(io.requery.Embedded::class.java)

/**
 * 해당 필드나 메소드가 entity들의 association을 나타내는 requery의 annotation이 있는지 여부
 */
fun AnnotatedElement.isAssociatedAnnotatedElement(): Boolean =
    isAnnotationPresent(io.requery.OneToOne::class.java) ||
    isAnnotationPresent(io.requery.OneToMany::class.java) ||
    isAnnotationPresent(io.requery.ManyToOne::class.java) ||
    isAnnotationPresent(io.requery.ManyToMany::class.java)

/**
 * 엔티티의 특정 필드에 대한 [NamedExpression]의 캐시를 관리합니다.
 */
private val classKeyExpressions = ConcurrentHashMap<Class<out Any>, NamedExpression<out Any>>()
/**
 * Key = 엔티티의 명+필드명, Value=[NamedExpression]의 캐시
 */
private val classPropertyExpressions = ConcurrentHashMap<String, NamedExpression<out Any>?>()

val UNKNOWN_KEY_EXPRESSION: NamedExpression<*> = NamedExpression.of("Unknown", Any::class.java)

/**
 * 엔티티에서 `@Key` annotation이 지정될 속성/필드를 이용하는 [NamedExpression]을 생성합니다.
 * @param V 속성/필드의 수형
 */
fun <V : Any> KClass<out Any>.getKeyExpression(): NamedExpression<V> =
    this.java.getKeyExpression()

/**
 * 엔티티에서 `@Key` annotation이 지정될 속성/필드를 이용하는 [NamedExpression]을 생성합니다.
 * @param V 속성/필드의 수형
 */
@Suppress("UNCHECKED_CAST")
fun <V : Any> Class<out Any>.getKeyExpression(): NamedExpression<V> {

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

/**
 * 엔티티에 대해 지정한 속성명으로 [NamedExpression]을 빌드합니다.
 * @param propertyName
 */
@Suppress("UNCHECKED_CAST")
fun Class<out Any>.getExpression(propertyName: String): NamedExpression<*>? {

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

/**
 * 지정한 엔티티 수형으로부터 실제 엔티티 명을 추출합니다.
 * Java에서는 `Abstract` 접두사, Kotlin에서는 `Entity` 접미사를 제거하고 반환합니다.
 */
fun Class<*>.getRequeryEntityName(): String {
    return when {
        simpleName.contains("Abstract") -> name.removePrefix("Abstract")
        simpleName.contains("Entity")   -> name.removeSuffix("Entity")
        else                            -> simpleName
    }
}

/**
 * Kotlin으로 정의된 Entity의 경우 [Method]로부터 field name을 추출합니다. (get/set 접두사를 제거합니다)
 */
fun Method.extractGetterSetter(): String {
    return when {
        name.startsWith("get") -> name.removePrefix("get").decapitalize()
        name.startsWith("set") -> name.removePrefix("set").decapitalize()
        else                   -> name.decapitalize()
    }
}
