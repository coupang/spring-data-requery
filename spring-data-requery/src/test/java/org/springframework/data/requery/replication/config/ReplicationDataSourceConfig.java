package org.springframework.data.requery.replication.config;

import io.requery.meta.EntityModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.configs.AbstractRequeryConfiguration;
import org.springframework.data.requery.replication.domain.Models;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ReplicationConfig
 *
 * @author debop
 * @since 18. 11. 21
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class ReplicationDataSourceConfig extends AbstractRequeryConfiguration {

    @Override
    public EntityModel getEntityModel() {
        return Models.REPLICATIONS;
    }
}
