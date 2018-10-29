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
Benchmark                           Mode  Cnt   Score   Error  Units
JpaBulkUpdateBenchmark.update1_000  avgt   10  34.293 ± 8.807  ms/op
JpaBulkUpdateBenchmark.updateTen    avgt   10   0.748 ± 0.431  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class JpaBulkUpdateBenchmark {

    private static EntityManagerFactory emf = JpaUtils.getEntityManagerFactory();
    private static PlatformTransactionManager tm = new JpaTransactionManager(emf);
    private static TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    @State(Scope.Thread)
    public static class FullLogTen {
        private EntityManager em;
        private TransactionStatus ts;
        private List<FullLog> insertedFullLogs;
        private List<FullLog> updateFullLogs;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedFullLogs = randomFullLogs(10);
            updateFullLogs = randomFullLogs(10);
            em.getTransaction().begin();
            insertLogs(em, insertedFullLogs);
            em.getTransaction().commit();
        }
        @TearDown(Level.Invocation)
        public void teardown() {
            if (tm != null) {
                tm.commit(ts);
            }
            if (em != null) {
                em.close();
            }
        }
    }
    @State(Scope.Thread)
    public static class FullLog1_000 {
        private EntityManager em;
        private TransactionStatus ts;
        private List<FullLog> insertedFullLogs;
        private List<FullLog> updateFullLogs;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedFullLogs = randomFullLogs(1_000);
            updateFullLogs = randomFullLogs(1_000);
            em.getTransaction().begin();
            insertLogs(em, insertedFullLogs);
            em.getTransaction().commit();
        }
        @TearDown(Level.Invocation)
        public void teardown() {
            if (tm != null) {
                tm.commit(ts);
            }
            if (em != null) {
                em.close();
            }
        }
    }
    @Benchmark
    public void updateTen(FullLogTen fullLogTen) {
        fullLogTen.em.getTransaction().begin();
        updateFullLogs(fullLogTen.em, fullLogTen.insertedFullLogs, fullLogTen.updateFullLogs);
        fullLogTen.em.getTransaction().commit();
    }
    @Benchmark
    public void update1_000(FullLog1_000 fullLog1_000) {
        fullLog1_000.em.getTransaction().begin();
        updateFullLogs(fullLog1_000.em, fullLog1_000.insertedFullLogs, fullLog1_000.updateFullLogs);
        fullLog1_000.em.getTransaction().commit();
    }
    private void updateFullLogs(EntityManager em, List<FullLog> fullLogs, List<FullLog> updateLogs) {
        for (int i=0; i < fullLogs.size(); i++) {
            FullLog fetched = em.find(FullLog.class, fullLogs.get(i).getId());
            fetched.setSystemId(updateLogs.get(i).getSystemId());
            fetched.setSystemName(updateLogs.get(i).getSystemName());
            fetched.setLogLevel(updateLogs.get(i).getLogLevel());
            em.persist(fetched);
        }
        em.flush();
    }
    private static void insertLogs(EntityManager em, List<FullLog> fullLogs) {
        for (int i = 0; i < fullLogs.size(); i++) {
            em.persist(fullLogs.get(i));
            if (i % 50 == 0) {
                em.flush();
            }
        }
        em.flush();
    }
}
