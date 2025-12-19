package org.plovdev.audioengine.loaders;

import java.nio.file.Path;

public final class PathLocator {
    private Path path;

    public PathLocator() {
    }

    public PathLocator(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}