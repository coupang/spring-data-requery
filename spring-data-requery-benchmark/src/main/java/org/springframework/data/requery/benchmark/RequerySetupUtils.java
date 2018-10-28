package org.springframework.data.requery.benchmark;

import io.requery.cache.EmptyEntityCache;
import io.requery.sql.*;
import org.springframework.data.requery.benchmark.model.Models;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * @author Diego on 2018. 10. 21..
 */
public class RequerySetupUtils {

    private static DataSource dataSource = getDataSource();
    private static Configuration configuration = getConfiguration();
    public static EntityDataStore<Object> dataStore = getDataStore();

    @Nonnull
    public static Configuration getConfiguration() {
        return new ConfigurationBuilder(dataSource, Models.DEFAULT)
            .setBatchUpdateSize(100)
            .setStatementCacheSize(1024)
            .setEntityCache(new EmptyEntityCache())
            .build();
    }

    @Nonnull
    public static DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setName("test")
            .setType(EmbeddedDatabaseType.H2)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .build();
    }

    @Nonnull
    public static EntityDataStore<Object> getDataStore() {

        SchemaModifier schemaModifier = new SchemaModifier(configuration);
        schemaModifier.createTables(TableCreationMode.CREATE_NOT_EXISTS);

        return new EntityDataStore<>(configuration);
    }
}
