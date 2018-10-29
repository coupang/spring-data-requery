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

package org.springframework.data.jpa.benchmark.simple;

import org.openjdk.jmh.annotations.*;
import org.springframework.data.jpa.benchmark.JpaUtils;
import org.springframework.data.jpa.benchmark.model.FullLog;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.jpa.benchmark.model.FullLogHelper.randomFullLogs;

/*
Benchmark                                                            Mode  Cnt         Score          Error   Units
JpaBulkInsertBenchmark.ins1_000                                      avgt   10      2013.185 ±      313.365   ms/op
JpaBulkInsertBenchmark.ins1_000:·gc.alloc.rate                       avgt   10       443.366 ±      178.477  MB/sec
JpaBulkInsertBenchmark.ins1_000:·gc.alloc.rate.norm                  avgt   10  14405982.383 ± 16311185.602    B/op
JpaBulkInsertBenchmark.ins1_000:·gc.churn.PS_Eden_Space              avgt   10       426.484 ±      223.767  MB/sec
JpaBulkInsertBenchmark.ins1_000:·gc.churn.PS_Eden_Space.norm         avgt   10  14424701.529 ± 17619708.841    B/op
JpaBulkInsertBenchmark.ins1_000:·gc.churn.PS_Survivor_Space          avgt   10         8.681 ±       22.724  MB/sec
JpaBulkInsertBenchmark.ins1_000:·gc.churn.PS_Survivor_Space.norm     avgt   10    356634.530 ±  1097785.436    B/op
JpaBulkInsertBenchmark.ins1_000:·gc.count                            avgt   10        16.000                 counts
JpaBulkInsertBenchmark.ins1_000:·gc.time                             avgt   10      3497.000                     ms
JpaBulkInsertBenchmark.ins5_000                                      avgt   10     45977.002 ±     1597.808   ms/op
JpaBulkInsertBenchmark.ins5_000:·gc.alloc.rate                       avgt   10       377.794 ±      176.990  MB/sec
JpaBulkInsertBenchmark.ins5_000:·gc.alloc.rate.norm                  avgt   10  55202521.812 ± 67518291.770    B/op
JpaBulkInsertBenchmark.ins5_000:·gc.churn.PS_Eden_Space              avgt   10       392.609 ±      232.984  MB/sec
JpaBulkInsertBenchmark.ins5_000:·gc.churn.PS_Eden_Space.norm         avgt   10  56195258.557 ± 70153285.301    B/op
JpaBulkInsertBenchmark.ins5_000:·gc.churn.PS_Survivor_Space          avgt   10        17.062 ±       24.662  MB/sec
JpaBulkInsertBenchmark.ins5_000:·gc.churn.PS_Survivor_Space.norm     avgt   10   3073810.792 ±  5279224.907    B/op
JpaBulkInsertBenchmark.ins5_000:·gc.count                            avgt   10        37.000                 counts
JpaBulkInsertBenchmark.ins5_000:·gc.time                             avgt   10     12583.000                     ms
JpaBulkInsertBenchmark.insTen                                        avgt   10        22.866 ±        5.673   ms/op
JpaBulkInsertBenchmark.insTen:·gc.alloc.rate                         avgt   10       494.445 ±       85.738  MB/sec
JpaBulkInsertBenchmark.insTen:·gc.alloc.rate.norm                    avgt   10   1084455.585 ±   555689.086    B/op
JpaBulkInsertBenchmark.insTen:·gc.churn.Compressed_Class_Space       avgt   10         0.002 ±        0.011  MB/sec
JpaBulkInsertBenchmark.insTen:·gc.churn.Compressed_Class_Space.norm  avgt   10        13.403 ±       64.077    B/op
JpaBulkInsertBenchmark.insTen:·gc.churn.Metaspace                    avgt   10         0.007 ±        0.031  MB/sec
JpaBulkInsertBenchmark.insTen:·gc.churn.Metaspace.norm               avgt   10        36.895 ±      176.390    B/op
JpaBulkInsertBenchmark.insTen:·gc.churn.PS_Eden_Space                avgt   10       462.815 ±      410.843  MB/sec
JpaBulkInsertBenchmark.insTen:·gc.churn.PS_Eden_Space.norm           avgt   10   1101421.543 ±  1558853.986    B/op
JpaBulkInsertBenchmark.insTen:·gc.churn.PS_Survivor_Space            avgt   10         1.078 ±        3.102  MB/sec
JpaBulkInsertBenchmark.insTen:·gc.churn.PS_Survivor_Space.norm       avgt   10      3737.294 ±    14001.354    B/op
JpaBulkInsertBenchmark.insTen:·gc.count                              avgt   10        10.000                 counts
JpaBulkInsertBenchmark.insTen:·gc.time                               avgt   10       488.000                     ms
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class JpaBulkInsertBenchmark {

    private static EntityManagerFactory emf = JpaUtils.getEntityManagerFactory();
    private static PlatformTransactionManager tm = new JpaTransactionManager(emf);
    private static TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    private EntityManager em;
    private TransactionStatus ts;

    @Setup
    public void setup() {
        ts = tm.getTransaction(transactionDefinition);
        em = emf.createEntityManager();
        em.joinTransaction();
    }

    @TearDown
    public void teardown() {
        if (tm != null) {
            tm.commit(ts);
        }
        if (em != null) {
            em.close();
        }
    }


    @Benchmark
    public void insTen() {
        insertLogs(10);
    }

    @Benchmark
    public void ins1_000() {
        insertLogs(1_000);
    }

    @Benchmark
    public void ins5_000() {
        insertLogs(5_000);
    }

//    @Benchmark
//    public void ins10_000() {
//        insertLogs(10_000);
//    }

//    @Benchmark
//    public void ins50_000() {
//        insertLogs(50_000);
//    }

    private void insertLogs(int count) {
        List<FullLog> fullLogs = randomFullLogs(count);

        for (int i = 0; i < fullLogs.size(); i++) {
            em.persist(fullLogs.get(i));
            if (i % 50 == 0) {
                em.flush();
            }
        }
    }
}
