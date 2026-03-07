package org.plovdev.audioengine.profiler.benchmarking;

import org.jetbrains.annotations.NotNull;

public record ProfileResult<V>(long executionDelay, V result) {
    @Override
    public @NotNull String toString() {
        return String.format("[Executed by: %s] - %s", (executionDelay / 1_000_000), result);
    }
}