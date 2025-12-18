package org.plovdev.audioengine.utils;

import org.plovdev.audioengine.loaders.TrackLoaderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class TrackLoaderSearcher {

    /**
     * Discovers all TrackLoaderManager implementations using ServiceLoader.
     * Returns empty list if no implementations found.
     */
    public static List<TrackLoaderManager> getSearchedLoaders() {
        List<TrackLoaderManager> loaders = new ArrayList<>();
        try {
            ServiceLoader<TrackLoaderManager> serviceLoader = ServiceLoader.load(TrackLoaderManager.class);
            for (TrackLoaderManager loader : serviceLoader) {
                System.out.println("Found loader");
                loaders.add(loader);
            }
        } catch (Exception e) {
            System.err.println("Failed to load TrackLoaderManager services: " + e.getMessage());
        }

        return loaders;
    }
}