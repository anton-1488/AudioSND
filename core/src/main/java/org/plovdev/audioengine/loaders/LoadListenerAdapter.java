package org.plovdev.audioengine.loaders;

import org.plovdev.audioengine.exceptions.loaders.TrackLoadException;

/**
 * Listener adapter for track loading events.
 *
 * @author Anton
 * @version 1.0
 * @since 1.0
 */
public abstract class LoadListenerAdapter implements LoadListener {
    /**
     * Called when loading starts.
     *
     * @param total total size in bytes, or {@code -1} if unknown
     */
    @Override
    public void onLoadStarted(long total) {

    }

    /**
     * Called when loading completes successfully.
     */
    @Override
    public void onLoadFinished() {

    }

    /**
     * Called when loading fails.
     *
     * @param error detailed exception describing the failure
     */
    @Override
    public void onLoadFailed(TrackLoadException error) {

    }
}
