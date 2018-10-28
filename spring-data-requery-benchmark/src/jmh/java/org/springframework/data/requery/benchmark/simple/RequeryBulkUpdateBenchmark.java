package org.springframework.data.requery.benchmark.simple;

import io.requery.sql.EntityDataStore;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.requery.benchmark.RequerySetupUtils;
import org.springframework.data.requery.benchmark.model.FullLog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.requery.benchmark.model.FullLogHelper.randomFullLogs;

/*
Benchmark                               Mode  Cnt    Score    Error  Units
RequeryBulkUpdateBenchmark.update10     avgt   10    1.565 ±  0.388  ms/op
RequeryBulkUpdateBenchmark.update1_000  avgt   10  107.849 ± 20.067  ms/op
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
        dataStore = RequerySetupUtils.dataStore;
    }
    @State(Scope.Thread)
    public static class FullLog10 {
        private List<FullLog> insertedFullLogs;
        private List<FullLog> updateFullLogs;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkUpdateBenchmark benchmark) {
            updateFullLogs = randomFullLogs(10);
            insertedFullLogs = randomFullLogs(10);
            benchmark.dataStore.insert(insertedFullLogs);
            for(int i=0; i < insertedFullLogs.size(); i++) {
                insertedFullLogs.get(i).setSystemId(updateFullLogs.get(i).getSystemId());
                insertedFullLogs.get(i).setSystemName(updateFullLogs.get(i).getSystemName());
                insertedFullLogs.get(i).setLogLevel(updateFullLogs.get(i).getLogLevel());
            }
        }
    }
    @State(Scope.Thread)
    public static class FullLog1_000 {
        private List<FullLog> insertedFullLogs;
        private List<FullLog> updateFullLogs;
        @Setup(Level.Invocation)
        public void setup(RequeryBulkUpdateBenchmark benchmark) {
            updateFullLogs = randomFullLogs(1_000);
            insertedFullLogs = randomFullLogs(1_000);
            benchmark.dataStore.insert(insertedFullLogs);
            for(int i=0; i < insertedFullLogs.size(); i++) {
                insertedFullLogs.get(i).setSystemId(updateFullLogs.get(i).getSystemId());
                insertedFullLogs.get(i).setSystemName(updateFullLogs.get(i).getSystemName());
                insertedFullLogs.get(i).setLogLevel(updateFullLogs.get(i).getLogLevel());
            }
        }
    }

    @Benchmark
    public void update10(FullLog10 fullLog10) {
        dataStore.update(fullLog10.insertedFullLogs);
    }
    @Benchmark
    public void update1_000(FullLog1_000 fullLogId1_000) {
        dataStore.update(fullLogId1_000.insertedFullLogs);
    }

//    @Benchmark
//    public void upsertTen(FullLogTen fullLogIdTen) {
//        dataStore.upsert(fullLogIdTen.insertedFullLogs);
//    }
//
//    @Benchmark
//    public void upsert1_000(FullLog1_000 fullLogId1_000) {
//        dataStore.upsert(fullLogId1_000.insertedFullLogs);
//    }

}
