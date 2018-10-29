package org.springframework.data.requery.benchmark.complex;

import io.requery.sql.EntityDataStore;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.requery.benchmark.RequerySetupUtils;
import org.springframework.data.requery.benchmark.model.TagType;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.requery.benchmark.model.RandomTagHelper.*;

/*
Benchmark                               Mode  Cnt   Score    Error  Units
RequeryBulkDeleteBenchmark.delete10     avgt   10   1.237 ±  0.099  ms/op
RequeryBulkDeleteBenchmark.delete1_000  avgt   10  75.259 ± 15.801  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class RequeryBulkDeleteBenchmark {

    private EntityDataStore<Object> dataStore;
    @Setup
    public void setup() {
        dataStore = RequerySetupUtils.getDataStore();
    }

    @State(Scope.Thread)
    public static class Tag10 {
        private List<TagType> insertedTags;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkDeleteBenchmark benchmark) {
            insertedTags = randomTags(10);
            benchmark.dataStore.insert(insertedTags);
        }
    }

    @State(Scope.Thread)
    public static class Tag1_000 {
        private List<TagType> insertedTags;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkDeleteBenchmark benchmark) {
            insertedTags = randomTags(1_000);
            benchmark.dataStore.insert(insertedTags);
        }
    }

    @Benchmark
    public void deleteTen(Tag10 tag10) {
        dataStore.delete(tag10.insertedTags);
    }
    @Benchmark
    public void delete1_000(Tag1_000 tag1_000) {
        dataStore.delete(tag1_000.insertedTags);
    }
}
