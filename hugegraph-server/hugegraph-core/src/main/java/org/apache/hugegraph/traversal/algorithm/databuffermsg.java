package org.apache.hugegraph.traversal.algorithm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
public class databuffermsg {
    private BlockingQueue<DataBuffer> queue;

    private int datasize;


    public databuffermsg() {
        this.queue = new LinkedBlockingQueue<>();
        this.datasize = 0 ;
    }

    public void enqueue(DataBuffer dataBuffer) {
        try {
            queue.put(dataBuffer);
            datasize++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while adding dataBuffer to the queue", e);
        }
    }

    public DataBuffer dequeue() {
        try {
            DataBuffer buffer = queue.take();
            datasize--;
            return buffer;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while removing dataBuffer from the queue", e);
        }
    }

    public int getDatasize(){
        return datasize;
    }

}
