package org.springframework.data.requery.benchmark.model;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Diego on 28/10/2018.
 */
public class FullLogHelper {

    private FullLogHelper() {}

    private static Random rnd = new Random(System.currentTimeMillis());

    private static FullLog randomFullLog() {
        FullLog fullLog = new FullLog();
        fullLog.setCreateAt(new Date());
        fullLog.setSystemId("SystemId:" + rnd.nextInt(1000));
        fullLog.setSystemName("SystemName:" + rnd.nextInt(1000));
        fullLog.setLogLevel(rnd.nextInt(5));
        fullLog.setThreadName("main-" + rnd.nextInt(16));
        fullLog.setLogMessage("동해물과 백두산이 마르고 닳도록, 동해물과 백두산이 마르고 닳도록, 동해물과 백두산이 마르고 닳도록");

        return fullLog;
    }

    public static List<FullLog> randomFullLogs(int count) {
        return IntStream
            .range(0, count)
            .mapToObj(it -> randomFullLog())
            .collect(Collectors.toList());
    }
}
