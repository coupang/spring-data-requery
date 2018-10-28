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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Transient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Requery용 {@link DataSourceTransactionManager}
 *
 * @author debop
 * @since 18. 6. 14
 * @deprecated Use DataSourceTransactionManager.
 */
@Deprecated
@Slf4j
public class RequeryTransactionManager extends DataSourceTransactionManager {
    private static final long serialVersionUID = 3291422158479490099L;

    @Transient
    private transient EntityDataStore entityDataStore;

    public RequeryTransactionManager(@NotNull final EntityDataStore entityDataStore,
                                     @NotNull final DataSource dataSource) {
        super(dataSource);
        this.entityDataStore = entityDataStore;
    }

    @Override
    protected void doBegin(@NotNull final Object transaction, @NotNull final TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        log.debug("Begin transaction... definition={}", definition);

        TransactionIsolation isolation = getTransactionIsolation(definition.getIsolationLevel());

        if (isolation != null) {
            entityDataStore.transaction().begin(isolation);
        } else {
            entityDataStore.transaction().begin();
        }
    }

    @Override
    protected void doCommit(@NotNull final DefaultTransactionStatus status) {
        if (entityDataStore.transaction().active()) {
            log.debug("Commit transaction. status={}", status.getTransaction());
            entityDataStore.transaction().commit();
        }
        super.doCommit(status);
    }

    @Override
    protected void doRollback(@NotNull final DefaultTransactionStatus status) {
        if (entityDataStore.transaction().active()) {
            log.debug("Rollback transaction. status={}", status);
            entityDataStore.transaction().rollback();
        }
        super.doRollback(status);
    }

    @NotNull
    @Override
    protected Object doSuspend(@NotNull final Object transaction) {
        log.debug("Suspend transaction. transaction={}", transaction);
        return super.doSuspend(transaction);
    }

    @Override
    protected void doResume(@Nullable final Object transaction,
                            @NotNull final Object suspendedResources) {
        log.debug("Resume transaction. transaction={}, suspendedResources={}", transaction, suspendedResources);
        super.doResume(transaction, suspendedResources);
    }

    private @Nullable TransactionIsolation getTransactionIsolation(int isolationLevel) {
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
