package org.plovdev.audioengine.devices;

/**
 * AudioDevice lifecycle status.
 *
 * @author Anton
 * @version 1.0
 */
public enum AudioDeviceStatus {
    OPENING, OPENED, RUNNING, CLOSING, CLOSED, DESTROYED, UNAVAILABLE, ERROR
}