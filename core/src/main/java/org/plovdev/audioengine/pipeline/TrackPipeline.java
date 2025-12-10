package org.plovdev.audioengine.pipeline;

import java.util.List;

public interface TrackPipeline extends AutoCloseable {
    void openPipline();

    void addPiplineNode(PipelineNode node);
    void removePiplineNode();
    List<PipelineNode> getNodes();

    void startPipline();

    @Override
    void close();
}