package org.plovdev.audioengine.profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionBenchmarker {
    private static final Logger log = LoggerFactory.getLogger(ExecutionBenchmarker.class);

    public static long testExecutionDelay(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();

        return end - start;
    }
}