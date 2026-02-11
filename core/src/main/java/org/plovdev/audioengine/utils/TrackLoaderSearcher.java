package org.plovdev.audioengine.utils;

import org.plovdev.audioengine.loaders.TrackLoaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class TrackLoaderSearcher {
    private static final Logger log = LoggerFactory.getLogger(TrackLoaderSearcher.class);

    /**
     * Discovers all TrackLoaderManager implementations using ServiceLoader.
     * Returns empty list if no implementations found.
     */
    public static List<TrackLoaderManager> searchAvailableTrackLoaderManagers() {
        List<TrackLoaderManager> loaders = new ArrayList<>();
        try {
            ServiceLoader<TrackLoaderManager> serviceLoader = ServiceLoader.load(TrackLoaderManager.class);
            for (TrackLoaderManager loader : serviceLoader) {
                loaders.add(loader);
            }
        } catch (ServiceConfigurationError e) {
            log.error("Failed to load TrackLoaderManager services: ", e);
        }

        return List.copyOf(loaders);
    }
}