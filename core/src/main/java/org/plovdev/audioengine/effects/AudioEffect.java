package org.plovdev.audioengine.effects;

import org.jetbrains.annotations.NotNull;
import org.plovdev.audioengine.format.TrackFormat;

import java.nio.ByteBuffer;

/**
 * Audio effect for real-time stream processing.
 * <p>
 * Typical lifecycle:
 * <ol>
 *     <li>Call {@link #setup(TrackFormat)} once</li>
 *     <li>Call {@link #process(ByteBuffer)} repeatedly with audio chunks</li>
 * </ol>
 * </p>
 * Implementation may allocate native resources during setup
 * and release them during finalization or explicitly via implementation-specific method.
 */
public interface AudioEffect {

    /**
     * Initializes the effect with the specified audio format.
     * Must be called exactly once before any {@link #process(ByteBuffer)} calls.
     *
     * @param format audio format (sample rate, channels, etc.)
     * @throws IllegalStateException if already initialized
     */
    void setup(@NotNull TrackFormat format);

    /**
     * Applies the effect to a single audio chunk.
     * <p>
     * Implementation must not rely on input buffer's current position.
     * Always call {@link ByteBuffer#rewind()} or {@link ByteBuffer#position(int)}
     * if sequential reading from start is required.
     * </p>
     * <p>
     * Output buffer always has position = 0, limit = data size.
     * Output buffer may be the same instance as input or a new one.
     * Caller is responsible for discarding/recycling the returned buffer.
     * </p>
     *
     * @param source input audio chunk
     * @return processed audio chunk with position = 0, limit = data size
     */
    @NotNull
    ByteBuffer process(@NotNull ByteBuffer source);
}