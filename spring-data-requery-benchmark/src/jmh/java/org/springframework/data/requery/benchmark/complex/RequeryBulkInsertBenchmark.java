package org.springframework.data.requery.benchmark.complex;

import io.requery.sql.EntityDataStore;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.requery.benchmark.RequerySetupUtils;
import org.springframework.data.requery.benchmark.model.TagType;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.requery.benchmark.model.RandomTagHelper.randomTags;

/*
Benchmark                            Mode  Cnt     Score     Error  Units
RequeryBulkInsertBenchmark.ins10     avgt   10    15.072 ±  38.211  ms/op
RequeryBulkInsertBenchmark.ins1_000  avgt   10  1295.558 ± 179.435  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class RequeryBulkInsertBenchmark {

    private void insertTags(int count) {
        List<TagType> tagTypes = randomTags(count);
        dataStore.insert(tagTypes);
    }

    private EntityDataStore<Object> dataStore;

    @Setup
    public void setup() {
        dataStore = RequerySetupUtils.getDataStore();
    }

    @Benchmark
    public void ins10() {
        insertTags(10);
    }

    @Benchmark
    public void ins1_000() {
        insertTags(1_000);
    }

}
