package org.springframework.data.requery.kotlin.coroutines

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Requery용 CoroutineScope
 * @author debop (Sunghyouk Bae)
 */
object RequeryScope : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default // newSingleThreadContext("requery")
}