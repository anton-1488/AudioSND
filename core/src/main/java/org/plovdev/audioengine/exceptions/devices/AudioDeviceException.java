package org.plovdev.audioengine.exceptions.devices;

/**
 * Default audio dvice processing error.
 *
 * @author Anton
 * @version 1.0
 */
public class AudioDeviceException extends RuntimeException {
    public AudioDeviceException(String message) {
        super(message);
    }
}