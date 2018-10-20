package org.springframework.data.requery.kotlin.coroutines

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlin.coroutines.experimental.CoroutineContext

/**
 * 지정한 이름을 갖는 Single thread 용 [CoroutineScope]를 만듭니다.
 * 이는 JDBC의 Transaction 작업 시 유용하게 사용됩니다.
 *
 * @param name name of coroutine context to be created
 */
fun newSingleThreadCoroutineScope(name: String): CoroutineScope {
    return object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = newSingleThreadContext(name)

    }
}

fun newDefaultCoroutineScope(): CoroutineScope {
    return object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default
    }
}
