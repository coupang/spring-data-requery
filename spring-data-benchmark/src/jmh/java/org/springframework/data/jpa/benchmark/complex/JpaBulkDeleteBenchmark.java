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
import java.util.stream.Collectors;

import static org.springframework.data.jpa.benchmark.model.RandomTagHelper.*;

/*
Benchmark                           Mode  Cnt    Score    Error  Units
JpaBulkDeleteBenchmark.delete10     avgt   10    2.632 ±  0.878  ms/op
JpaBulkDeleteBenchmark.delete1_000  avgt   10  215.549 ± 53.198  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class JpaBulkDeleteBenchmark {

    private static void insertTags(EntityManager em, List<TagType> tagTypes) {
        for (int i = 0; i < tagTypes.size(); i++) {
            em.persist(tagTypes.get(i));
            if (i % 50 == 0) {
                em.flush();
            }
        }
    }

    private void deleteTags(EntityManager em, List<Long> fullLogList) {
        for (Long fullLogId : fullLogList) {
            TagType fetched = em.find(TagType.class, fullLogId);
            em.remove(fetched);
        }
        em.flush();
    }

    @State(Scope.Thread)
    public static class Tag10 {
        private EntityManager em;
        private TransactionStatus ts;
        private List<TagType> insertedTags;
        private List<Long> insertedTagIds;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedTags = randomTags(10);
            em.getTransaction().begin();
            insertTags(em, insertedTags);
            em.getTransaction().commit();
            insertedTagIds= insertedTags.stream().map(TagType::getTagTypeId).collect(Collectors.toList());
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
    public static class Tag1_000 {
        private EntityManager em;
        private TransactionStatus ts;
        private List<TagType> insertedTags;
        private List<Long> insertedTagIds;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedTags = randomTags(1_000);
            em.getTransaction().begin();
            insertTags(em, insertedTags);
            em.getTransaction().commit();
            insertedTagIds= insertedTags.stream().map(TagType::getTagTypeId).collect(Collectors.toList());
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
    public void delete10(Tag10 tag10) {
        tag10.em.getTransaction().begin();
        deleteTags(tag10.em, tag10.insertedTagIds);
        tag10.em.getTransaction().commit();
    }
    @Benchmark
    public void delete1_000(Tag1_000 tag1_000) {
        tag1_000.em.getTransaction().begin();
        deleteTags(tag1_000.em, tag1_000.insertedTagIds);
        tag1_000.em.getTransaction().commit();
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
