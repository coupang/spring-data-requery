package org.springframework.data.requery.kotlin.repository.query

import io.requery.PersistenceException
import io.requery.query.Result
import io.requery.query.element.QueryElement
import mu.KLogging
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.ConfigurableConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.data.domain.Pageable
import org.springframework.data.requery.kotlin.applyPageable
import org.springframework.data.requery.kotlin.unwrap
import org.springframework.util.ClassUtils

/**
 * Coroutine 용 [RequeryQueryExecution]
 *
 * @author debop (Sunghyouk Bae)
 */
abstract class AbstractCoroutineRequeryQueryExecution {

    companion object : KLogging() {

        @JvmStatic
        private val CONVERSION_SERVICE: ConversionService by lazy {
            val conversionService = DefaultConversionService()

            // Blob to Byte array 로 하는 것은 BlobByteArrayConverter 를 사용하면 된다.
            // conversionService.addConverter(JpaResultConverters.BlobToByteArrayConverter.INSTANCE);

            conversionService.removeConvertible(Collection::class.java, Any::class.java)
            potentiallyRemoveOptionalConverter(conversionService)

            conversionService
        }

        @JvmStatic
        fun potentiallyRemoveOptionalConverter(conversoinService: ConfigurableConversionService) {

            val classLoader = RequeryQueryExecution::class.java.classLoader

            if(ClassUtils.isPresent("java.util.Optional", classLoader)) {
                try {
                    val optionalType = ClassUtils.forName("java.util.Optional", classLoader)
                    conversoinService.removeConvertible(Any::class.java, optionalType)
                } catch(e: ClassNotFoundException) {
                    // Nothing to do.
                } catch(e: LinkageError) {
                    // Nothing to do.
                }
            }
        }
    }

    suspend fun execute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Any? = try {
        doExecute(query, values)
    } catch(e: PersistenceException) {
        logger.error(e) { "Fail to execute query. return null. query=$query" }
        null
    }

    protected abstract suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Any?

    /**
     * method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
     */
    protected fun adjustPage(queryElement: QueryElement<out Any>,
                             domainClass: Class<out Any>,
                             pageable: Pageable): QueryElement<out Any> {

        // method name에서 paging을 유추할 수 있을 수 있기 때문에 추가로 paging을 하지 않는다.
        return if(queryElement.limit == null && queryElement.offset == null)
            queryElement.applyPageable(domainClass, pageable).unwrap()
        else
            queryElement
    }
}

internal class CoroutineCollectionExecution : AbstractCoroutineRequeryQueryExecution() {

    companion object : KLogging()

    override suspend fun doExecute(query: AbstractCoroutineRequeryQuery, values: Array<Any>): Any? {
        val result = query.createQueryElement(values).get() as Result<*>
        return result.toList()
    }
}