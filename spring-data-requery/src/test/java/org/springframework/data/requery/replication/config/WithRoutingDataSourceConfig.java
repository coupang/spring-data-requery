package org.springframework.data.requery.replication.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * {@link LazyConnectionDataSourceProxy} 와 {@link AbstractRoutingDataSource} 를 이용하여,
 * 필요에 따라 Master, Slave DB를 선택할 수 있도록 합니다.
 *
 * @author debop
 * @since 18. 11. 21
 */
@Slf4j
@Configuration
public class WithRoutingDataSourceConfig {

    @Autowired
    private ApplicationContext appContext;


    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean(name = { "routingDataSource" })
    public DataSource routingDataSource(@Qualifier("writeDataSource") DataSource writeDataSource) {
        final List<DataSource> readDataSources = Arrays
            .stream(appContext.getBeanNamesForType(DataSource.class))
            .filter(it -> it.startsWith("read"))
            .map(it -> appContext.getBean(it, DataSource.class))
            .collect(Collectors.toList());

        final int readDataSourceCount = readDataSources.size();

        AbstractRoutingDataSource dataSource = new AbstractRoutingDataSource() {

            private AtomicInteger slaveIndex = new AtomicInteger(0);

            @Override
            protected Object determineCurrentLookupKey() {
                Boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
                log.trace("Current is readOnly={}", isReadOnly);

                String lookupKey = "write";
                if (isReadOnly) {
                    int index = slaveIndex.getAndIncrement() % readDataSourceCount;
                    slaveIndex.compareAndSet(readDataSourceCount, 0);
                    lookupKey = "read_" + index;
                }

                log.trace("Current Lookup Key={}", lookupKey);
                return lookupKey;
            }
        };

        final Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("write", writeDataSource);

        for (int i = 0; i < readDataSourceCount; i++) {
            targetDataSources.put("read_" + i, readDataSources.get(i));
        }

        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(writeDataSource);

        return dataSource;
    }

    @Bean(name = { "writeDataSource" })
    public DataSource writeDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setName("routingWriteDB")
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .addScript("classpath:/replication/writedb.sql")
            .build();
    }

    @Bean(name = { "readDataSource1" })
    public DataSource readDataSource1() {
        return new EmbeddedDatabaseBuilder()
            .setName("routingReadDB1")
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .addScript("classpath:/replication/readdb.sql")
            .build();
    }

    @Bean(name = { "readDataSource2" })
    public DataSource readDataSource2() {
        return new EmbeddedDatabaseBuilder()
            .setName("routingReadDB2")
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .addScript("classpath:/replication/readdb.sql")
            .build();
    }

}
