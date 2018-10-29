package org.springframework.data.requery.benchmark.complex;

import io.requery.sql.EntityDataStore;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.requery.benchmark.RequerySetupUtils;
import org.springframework.data.requery.benchmark.model.AbstractTagValue;
import org.springframework.data.requery.benchmark.model.TagType;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.requery.benchmark.model.RandomTagHelper.randomTags;

/*
Benchmark                               Mode  Cnt    Score    Error  Units
RequeryBulkUpdateBenchmark.update10     avgt   10    2.905 ±  0.931  ms/op
RequeryBulkUpdateBenchmark.update1_000  avgt   10  130.982 ± 16.097  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class RequeryBulkUpdateBenchmark {

    private EntityDataStore<Object> dataStore;
    @Setup
    public void setup() {
        dataStore = RequerySetupUtils.getDataStore();
    }

    @State(Scope.Thread)
    public static class Tag10 {
        private List<TagType> insertedTags;
        private List<TagType> updateTags;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkUpdateBenchmark benchmark) {
            updateTags = randomTags(10);
            insertedTags = randomTags(10);
            benchmark.dataStore.insert(insertedTags);
            updateTags(insertedTags, updateTags);
        }
    }

    @State(Scope.Thread)
    public static class Tag1_000 {
        private List<TagType> insertedTags;
        private List<TagType> updateTags;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkUpdateBenchmark benchmark) {
            updateTags = randomTags(1_000);
            insertedTags = randomTags(1_000);
            benchmark.dataStore.insert(insertedTags);
            updateTags(insertedTags, updateTags);
        }
    }

    @Benchmark
    public void update10(Tag10 tag10) {
        dataStore.update(tag10.insertedTags);
    }

    @Benchmark
    public void update1_000(Tag1_000 tag1_000) {
        dataStore.update(tag1_000.insertedTags);
    }

    private static void updateTags(List<TagType> source, List<TagType> update) {
        for (int i=0; i < source.size(); i++) {
            TagType insTagType = source.get(i);

            insTagType.setTagTypeClass(update.get(i).getTagTypeClass());
            insTagType.setTagTypeName(update.get(i).getTagTypeName());
            insTagType.setModifiedAt(new Date());

            Iterator<AbstractTagValue> updateTagIter = update.get(i).getTagValues().iterator();
            for (AbstractTagValue tagValue : insTagType.getTagValues()) {
                AbstractTagValue newTagValue = updateTagIter.next();
                tagValue.setRep(newTagValue.getRep());
                tagValue.setSynonyms(newTagValue.getSynonyms());
                tagValue.setModifiedAt(new Date());
            }
        }
    }
}
