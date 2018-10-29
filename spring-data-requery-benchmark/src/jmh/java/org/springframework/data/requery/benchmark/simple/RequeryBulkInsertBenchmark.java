package org.springframework.data.requery.benchmark.simple;

import io.requery.sql.EntityDataStore;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.requery.benchmark.RequerySetupUtils;
import org.springframework.data.requery.benchmark.model.FullLog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.requery.benchmark.model.FullLogHelper.randomFullLogs;

/*
Benchmark                            Mode  Cnt    Score     Error  Units
RequeryBulkInsertBenchmark.ins10     avgt   10    1.805 ±   2.830  ms/op
RequeryBulkInsertBenchmark.ins1_000  avgt   10  115.965 ± 266.193  ms/op
 */
@BenchmarkMode(Mode.AverageTime)
@Threads(Threads.MAX)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
public class RequeryBulkInsertBenchmark {

    private EntityDataStore<Object> dataStore;
    @Setup
    public void setup() {
        dataStore = RequerySetupUtils.dataStore;
    }
    @Benchmark
    public void ins10() {
        insertLogs(10);
    }
    @Benchmark
    public void ins1_000() {
        insertLogs(1_000);
    }

//    @Benchmark
//    public void ins5_000() {
//        insertLogs(5_000);
//    }
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
        dataStore.insert(fullLogs);
    }
}
