package org.springframework.data.requery.kotlin.core

import io.requery.async.CompletableEntityStore
import io.requery.meta.QueryAttribute
import io.requery.query.Result
import io.requery.query.Selection

/**
 * AsyncRequeryOperations
 *
 * @author debop
 * @since 18. 11. 14
 */
interface AsyncRequeryOperations {

    @JvmDefault
    val dataStore: CompletableEntityStore<Any>

    @JvmDefault
    fun <E : Any> select(entityType: Class<E>): Selection<out Result<E>> = dataStore.select(entityType)

    @JvmDefault
    fun <E : Any> select(entityType: Class<E>, vararg attributes: QueryAttribute<E, *>): Selection<out Result<E>> =
        dataStore.select(entityType, *attributes)

}