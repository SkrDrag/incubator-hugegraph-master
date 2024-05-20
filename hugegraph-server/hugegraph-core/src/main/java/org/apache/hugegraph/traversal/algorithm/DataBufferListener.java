package org.apache.hugegraph.traversal.algorithm;

public interface DataBufferListener {
    void onDataBufferFull(DataBufferEvent event);
}
