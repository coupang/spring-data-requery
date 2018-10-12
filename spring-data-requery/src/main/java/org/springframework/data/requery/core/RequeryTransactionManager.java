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

package org.springframework.data.requery.core;

import io.requery.TransactionIsolation;
import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * RequeryTransactionManager
 *
 * @author debop
 * @since 18. 6. 14
 */
@Slf4j
public class RequeryTransactionManager extends DataSourceTransactionManager {
    private static final long serialVersionUID = 3291422158479490099L;

    private final EntityDataStore entityDataStore;

    public RequeryTransactionManager(EntityDataStore entityDataStore, DataSource dataSource) {
        super(dataSource);
        this.entityDataStore = entityDataStore;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

        log.debug("Begin transaction. definition={}", definition);

        TransactionIsolation isolation = getTransactionIsolation(definition.getIsolationLevel());
        if (isolation != null) {
            entityDataStore.transaction().begin(isolation);
        } else {
            entityDataStore.transaction().begin();
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        if (entityDataStore.transaction().active()) {
            try {
                entityDataStore.transaction().commit();
            } catch (Throwable ignored) {
                log.trace("Fail to commit in requery transaction", ignored);
            }
        }
        super.doCommit(status);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        if (entityDataStore.transaction().active()) {
            try {
                entityDataStore.transaction().rollback();
            } catch (Throwable ignored) {
                log.trace("Fail to rollback in requery transaction", ignored);
            }
        }
        super.doRollback(status);
    }

    @Override
    protected Object doSuspend(Object transaction) {
        return super.doSuspend(transaction);
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) {
        super.doResume(transaction, suspendedResources);
    }

    @Nullable
    private TransactionIsolation getTransactionIsolation(int isolationLevel) {
        switch (isolationLevel) {
            case Connection.TRANSACTION_NONE:
                return TransactionIsolation.NONE;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return TransactionIsolation.READ_UNCOMMITTED;
            case Connection.TRANSACTION_READ_COMMITTED:
                return TransactionIsolation.READ_COMMITTED;
            case Connection.TRANSACTION_REPEATABLE_READ:
                return TransactionIsolation.REPEATABLE_READ;
            case Connection.TRANSACTION_SERIALIZABLE:
                return TransactionIsolation.SERIALIZABLE;

            default:
                return null;
        }
    }
}
