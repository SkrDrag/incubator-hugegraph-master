package org.apache.hugegraph.traversal.algorithm;

public class DataBufferSubscriber implements DataBufferListener {
    @Override
    public void onDataBufferFull(DataBufferEvent event) {
        System.out.println("Received data buffer update, processing...");
        // process the updated dataBuffer
    }
}
