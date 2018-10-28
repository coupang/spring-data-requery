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
import java.util.stream.Collectors;

import static org.springframework.data.jpa.benchmark.model.FullLogHelper.randomFullLogs;

/*
Benchmark                           Mode  Cnt   Score   Error  Units
JpaBulkDeleteBenchmark.delete1_000  avgt   10  26.600 ± 4.811  ms/op
JpaBulkDeleteBenchmark.deleteTen    avgt   10   0.477 ± 0.039  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class JpaBulkDeleteBenchmark {

    private static EntityManagerFactory emf = JpaUtils.getEntityManagerFactory();
    private static PlatformTransactionManager tm = new JpaTransactionManager(emf);
    private static TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    @State(Scope.Thread)
    public static class FullLogTen {
        private EntityManager em;
        private TransactionStatus ts;
        private List<FullLog> insertedFullLogs;
        private List<Long> insertedFullLogIds;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedFullLogs = randomFullLogs(10);
            em.getTransaction().begin();
            insertLogs(em, insertedFullLogs);
            em.getTransaction().commit();
            insertedFullLogIds = insertedFullLogs.stream().map(FullLog::getId).collect(Collectors.toList());
        }
        @TearDown(Level.Invocation)
        public void tearDown() {
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
        private List<Long> insertedFullLogIds;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedFullLogs = randomFullLogs(1_000);
            em.getTransaction().begin();
            insertLogs(em, insertedFullLogs);
            em.getTransaction().commit();
            insertedFullLogIds = insertedFullLogs.stream().map(FullLog::getId).collect(Collectors.toList());
        }
        @TearDown(Level.Invocation)
        public void tearDown() {
            if (tm != null) {
                tm.commit(ts);
            }
            if (em != null) {
                em.close();
            }
        }
    }
    @Benchmark
    public void deleteTen(FullLogTen fullLogIdTen) {
        fullLogIdTen.em.getTransaction().begin();
        deleteFullLogs(fullLogIdTen.em, fullLogIdTen.insertedFullLogIds);
        fullLogIdTen.em.getTransaction().commit();
    }
    @Benchmark
    public void delete1_000(FullLog1_000 fullLog1_000) {
        fullLog1_000.em.getTransaction().begin();
        deleteFullLogs(fullLog1_000.em, fullLog1_000.insertedFullLogIds);
        fullLog1_000.em.getTransaction().commit();
    }
    private void deleteFullLogs(EntityManager em, List<Long> fullLogList) {
        for (Long fullLogId : fullLogList) {
            FullLog fetched = em.find(FullLog.class, fullLogId);
            em.remove(fetched);
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
