package org.springframework.data.jpa.benchmark.complex;

import org.openjdk.jmh.annotations.*;
import org.springframework.data.jpa.benchmark.JpaUtils;
import org.springframework.data.jpa.benchmark.model.TagType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.jpa.benchmark.model.RandomTagHelper.randomTags;

/*
Benchmark                        Mode  Cnt      Score      Error  Units
JpaBulkInsertBenchmark.ins10     avgt   10     54.822 ±   11.186  ms/op
JpaBulkInsertBenchmark.ins1_000  avgt   10  14021.263 ± 5012.470  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class JpaBulkInsertBenchmark {

    @Benchmark
    public void ins10() {
        insertTags(10);
    }

    @Benchmark
    public void ins1_000() {
        insertTags(1_000);
    }

    private void insertTags(int count) {
        List<TagType> tagTypes = randomTags(count);

        for (int i = 0; i < tagTypes.size(); i++) {
            em.persist(tagTypes.get(i));
            if (i % 50 == 0) {
                em.flush();
            }
        }
    }

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
}
