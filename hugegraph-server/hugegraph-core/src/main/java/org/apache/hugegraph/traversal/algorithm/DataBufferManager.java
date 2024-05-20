package org.apache.hugegraph.traversal.algorithm;

import java.util.HashMap;
import java.util.Map;

public class DataBufferManager {
    private Map<String, DataBuffer> buffers = new HashMap<>();

    public void createDataBuffer(String key, int totalsize,int blocksize) {
        buffers.put(key, new DataBuffer(totalsize,blocksize));
    }

    public DataBuffer getDataBuffer(String key) {
        return buffers.get(key);
    }
}
