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

package org.springframework.data.jpa.benchmark;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.benchmark.model.FullLog;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * JpaUtils
 *
 * @author debop
 */
@UtilityClass
public class JpaUtils {

    private static DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setName("jpa-benchmark")
            .setType(EmbeddedDatabaseType.H2)
            .ignoreFailedDrops(true)
            .build();
    }

    private static JpaVendorAdapter getJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        return adapter;
    }

    public static EntityManagerFactory getEntityManagerFactory() {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

        factory.setPackagesToScan(FullLog.class.getPackage().getName());
        factory.setJpaVendorAdapter(getJpaVendorAdapter());
        factory.setDataSource(getDataSource());

        factory.afterPropertiesSet();

        return factory.getObject();
    }
}
