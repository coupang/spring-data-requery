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

package org.springframework.data.requery.kotlin.repository.config

import mu.KotlinLogging
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus

/**
 * org.springframework.data.requery.repository.config.DelegatingTransactionManager
 *
 * @author debop
 */
class DelegatingTransactionManager(private val txManager: PlatformTransactionManager) : PlatformTransactionManager {

    companion object {
        private val log = KotlinLogging.logger { }
    }

    var transactionRequests: Int = 0
    var definition: TransactionDefinition? = null

    override fun getTransaction(definition: TransactionDefinition?): TransactionStatus {
        this.transactionRequests++
        this.definition = definition

        log.info("Get transaction. transactionRequests={}, definition={}", transactionRequests, definition)

        return txManager.getTransaction(definition)
    }

    override fun commit(status: TransactionStatus) {
        log.info("Commit transaction. status={}", status)
        txManager.commit(status)
    }

    override fun rollback(status: TransactionStatus) {
        log.info("Rollback transaction. status={}", status)
        txManager.rollback(status)
    }

    fun resetCount() {
        log.info("Reset transaction request.")
        this.transactionRequests = 0
        this.definition = null
    }
}