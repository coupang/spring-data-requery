package org.springframework.data.jpa.benchmark.complex;

import org.openjdk.jmh.annotations.*;
import org.springframework.data.jpa.benchmark.JpaUtils;
import org.springframework.data.jpa.benchmark.model.TagType;
import org.springframework.data.jpa.benchmark.model.TagValue;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.jpa.benchmark.model.RandomTagHelper.randomTags;

/*
Benchmark                           Mode  Cnt    Score     Error  Units
JpaBulkUpdateBenchmark.update10     avgt   10    3.699 ±   1.654  ms/op
JpaBulkUpdateBenchmark.update1_000  avgt   10  361.483 ± 122.749  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class JpaBulkUpdateBenchmark {

    private static void insertTags(EntityManager em, List<TagType> tagTypes) {
        for (int i = 0; i < tagTypes.size(); i++) {
            em.persist(tagTypes.get(i));
            if (i % 50 == 0) {
                em.flush();
            }
        }
    }

    private void updateTags(EntityManager em, List<TagType> tagTypes, List<TagType> updateTags) {
        for (int i=0; i < tagTypes.size(); i++) {
            TagType fetched = em.find(TagType.class, tagTypes.get(i).getTagTypeId());

            fetched.setTagTypeName(updateTags.get(i).getTagTypeName());
            fetched.setTagTypeClass(updateTags.get(i).getTagTypeClass());
            fetched.setModifiedAt(new Date());

            Iterator<TagValue> updateTagIter = updateTags.get(i).getTagValues().iterator();
            for (TagValue tagValue : fetched.getTagValues()) {
                TagValue newTagValue = updateTagIter.next();
                tagValue.setRep(newTagValue.getRep());
                tagValue.setSynonyms(newTagValue.getSynonyms());
                tagValue.setModifiedAt(new Date());
            }
            em.persist(fetched);
        }
        em.flush();
    }

    @State(Scope.Thread)
    public static class Tag10 {
        private EntityManager em;
        private TransactionStatus ts;
        private List<TagType> insertedTags;
        private List<TagType> updateTags;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedTags = randomTags(10);
            updateTags = randomTags(10);
            em.getTransaction().begin();
            insertTags(em, insertedTags);
            em.getTransaction().commit();
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
        private List<TagType> updateTags;
        @Setup(Level.Invocation)
        public void setup() {
            ts = tm.getTransaction(transactionDefinition);
            em = emf.createEntityManager();
            insertedTags = randomTags(1_000);
            updateTags = randomTags(1_000);
            em.getTransaction().begin();
            insertTags(em, insertedTags);
            em.getTransaction().commit();
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
    public void update10(Tag10 tag10) {
        tag10.em.getTransaction().begin();
        updateTags(tag10.em, tag10.insertedTags, tag10.updateTags);
        tag10.em.getTransaction().commit();
    }
    @Benchmark
    public void update1_000(Tag1_000 tag1_000) {
        tag1_000.em.getTransaction().begin();
        updateTags(tag1_000.em, tag1_000.insertedTags, tag1_000.updateTags);
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
