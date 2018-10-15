package org.springframework.data.requery.kotlin.repository.query

import mu.KLogging
import org.springframework.data.repository.query.QueryMethod
import org.springframework.data.requery.kotlin.coroutines.CoroutineRequeryOperations

/**
 * CoroutinePartTreeRequerQuery
 * @author debop (Sunghyouk Bae)
 */
class CoroutinePartTreeRequerQuery(queryMethod: RequeryQueryMethod,
                                   operations: CoroutineRequeryOperations)
    : AbstractCoroutineRequeryQuery(queryMethod, operations) {

    companion object : KLogging()

    //    private val tree: PartTree
    //    private val paramters: RequeryParameters
    //
    //    private val countQueryPreparer: CountQueryPreparer
    //    private val queryPreparer: QueryPreparer
    //    private val context: RequeryMappingContext

    init {
        val parameters = queryMethod.parameters

        TODO("Not implemented")
    }

    override fun getQueryMethod(): QueryMethod {
        TODO("not implemented")
    }

    override fun execute(parameters: Array<Any>): Any? {
        TODO("not implemented")
    }
}