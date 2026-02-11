package org.plovdev.audioengine.exceptions;

/**
 * Base exception for all audio engine errors.
 * <p>
 * All exceptions in the audio engine hierarchy extend this class.
 * It is a {@link RuntimeException}, so no mandatory try-catch is required,
 * but proper error handling is still recommended.
 * </p>
 *
 * <p>Common subclasses include:</p>
 * <ul>
 *   <li>{@link org.plovdev.audioengine.exceptions.devices.OpenAudioDeviceException} - device opening failed</li>
 *   <li>{@link org.plovdev.audioengine.exceptions.devices.CloseAudioDeviceException} - device closing failed</li>
 *   <li>{@link org.plovdev.audioengine.exceptions.loaders.UnsupportedTrackFormat} - loader not support this format</li>
 *   <li>{@link org.plovdev.audioengine.exceptions.effects.EffectException} - audio data processing error</li>
 *   <li>{@link org.plovdev.audioengine.exceptions.GenerationException} - audio generation exception</li>
 * </ul>
 *
 * @author Anton
 * @version 1.0
 * @see RuntimeException
 * @since 1.0
 */
public class AudioEngineException extends RuntimeException {
    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public AudioEngineException() {
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public AudioEngineException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public AudioEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of
     * {@code cause}).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public AudioEngineException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether the stack trace should
     *                           be writable
     * @since 1.7
     */
    public AudioEngineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}