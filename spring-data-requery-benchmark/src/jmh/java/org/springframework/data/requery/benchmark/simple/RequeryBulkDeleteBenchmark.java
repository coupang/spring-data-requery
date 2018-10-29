package org.springframework.data.requery.benchmark.simple;

import io.requery.sql.EntityDataStore;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.requery.benchmark.RequerySetupUtils;
import org.springframework.data.requery.benchmark.model.FullLog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.requery.benchmark.model.FullLogHelper.randomFullLogs;

/*
Benchmark                               Mode  Cnt   Score   Error  Units
RequeryBulkDeleteBenchmark.delete10     avgt   10   0.610 ± 0.033  ms/op
RequeryBulkDeleteBenchmark.delete1_000  avgt   10  15.380 ± 1.664  ms/op
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
        dataStore = RequerySetupUtils.dataStore;
    }
    @State(Scope.Thread)
    public static class FullLog10 {
        private List<FullLog> insertedFullLogs;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkDeleteBenchmark benchmark) {
            insertedFullLogs = randomFullLogs(10);
            benchmark.dataStore.insert(insertedFullLogs);
        }
    }
    @State(Scope.Thread)
    public static class FullLog1_000 {
        private List<FullLog> insertedFullLogs;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkDeleteBenchmark benchmark) {
            insertedFullLogs = randomFullLogs(1_000);
            benchmark.dataStore.insert(insertedFullLogs);
        }
    }
    @Benchmark
    public void delete10(FullLog10 fullLog10) {
        dataStore.delete(fullLog10.insertedFullLogs);
    }
    @Benchmark
    public void delete1_000(FullLog1_000 fullLogId1_000) {
        dataStore.delete(fullLogId1_000.insertedFullLogs);
    }
}
