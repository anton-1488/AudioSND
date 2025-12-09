package org.plovdev.audioengine.pipline;

import java.util.List;

public interface TrackPipline extends AutoCloseable {
    void openPipline();

    void addPiplineNode(PiplineNode node);
    void removePiplineNode();
    List<PiplineNode> getNodes();

    void startPipline();

    @Override
    void close();
}