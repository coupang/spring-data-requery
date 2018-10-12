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

package org.springframework.data.requery.kotlin.listeners

import io.requery.sql.BoundParameters
import io.requery.sql.EntityStateListener
import io.requery.sql.StatementListener
import mu.KLogging
import java.sql.Statement

/**
 * LogbackListener
 *
 * @author debop
 * @since 18. 7. 2
 */
class LogbackListener<T> @JvmOverloads constructor(val printStatement: Boolean = false)
    : EntityStateListener<T>, StatementListener {

    companion object : KLogging()

    override fun preInsert(entity: T) {
        logger.debug { "preInsert $entity" }
    }

    override fun preUpdate(entity: T) {
        logger.debug { "preUpdate $entity" }
    }

    override fun preDelete(entity: T) {
        logger.debug { "preDelete $entity" }
    }

    override fun postDelete(entity: T) {
        logger.debug { "postDelete $entity" }
    }

    override fun postInsert(entity: T) {
        logger.debug { "postInsert $entity" }
    }

    override fun postUpdate(entity: T) {
        logger.debug { "postUpdate $entity" }
    }

    override fun postLoad(entity: T) {
        logger.debug { "postLoad $entity" }
    }

    override fun afterExecuteUpdate(statement: Statement?, count: Int) {
        logger.debug { "afterExecuteUpdate count=[$count]" }
    }

    override fun beforeExecuteBatchUpdate(statement: Statement?, sql: String?) {
        statement?.let {
            logger.debug { "beforeExecuteBatchUpdate SQL:\n$sql" }
            if(printStatement)
                logger.debug { "$statement" }
        }
    }

    override fun afterExecuteQuery(statement: Statement?) {
        logger.debug { "afterExecuteQuery" }
    }

    override fun afterExecuteBatchUpdate(statement: Statement?, count: IntArray?) {
        logger.debug { "afterExecuteBatchUpdate count=[$count]" }
    }

    override fun beforeExecuteUpdate(statement: Statement?, sql: String?, parameters: BoundParameters?) {
        if(parameters != null && !parameters.isEmpty) {
            logger.debug { "beforeExecuteUpdate SQL:\n$sql ($parameters)" }
        } else {
            logger.debug { "beforeExecuteUpdate SQL:\n$sql" }
        }

        if(printStatement)
            logger.debug { "$statement" }
    }

    override fun beforeExecuteQuery(statement: Statement?, sql: String?, parameters: BoundParameters?) {
        if(parameters != null && !parameters.isEmpty) {
            logger.debug { "beforeExecuteQuery SQL:\n$sql ($parameters)" }
        } else {
            logger.debug { "beforeExecuteQuery SQL:\n$sql" }
        }

        if(printStatement)
            logger.debug { "$statement" }
    }
}