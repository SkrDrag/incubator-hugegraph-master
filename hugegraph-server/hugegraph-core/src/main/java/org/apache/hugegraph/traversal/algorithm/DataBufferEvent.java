package org.apache.hugegraph.traversal.algorithm;

public class DataBufferEvent {
    private final DataBuffer dataBuffer;

    public DataBufferEvent(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    public DataBuffer getDataBuffer() {
        return dataBuffer;
    }
}

