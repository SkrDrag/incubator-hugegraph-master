package org.apache.hugegraph.traversal.algorithm;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
public class msgQueue {
    private final BlockingQueue<Integer> queue;
    private int iter1;
    private int iter2;

    public msgQueue() {
        // 初始化队列，大小为1
        this.queue = new ArrayBlockingQueue<>(1);
        this.iter1 = iter1;
        this.iter2 = iter2;
    }


    public void setIter1(int depth){
        this.iter1 = depth;
    }

    public int getIter1(){
        return iter1;
    }

    public void decrementIter1() {
        iter1--;
    }

    public void setIter2(int depth){
        this.iter2 = depth;
    }

    public int getIter2(){
        return iter1;
    }

    public void decrementIter2() {
        iter2--;
    }

    // 向队列添加一个顶点数量消息
    public void putVertexCount(int vertexCount) throws InterruptedException {
        queue.put(vertexCount); // 如果队列满，等待直到有空间
    }

    // 从队列获取一个顶点数量消息
    public int takeVertexCount() throws InterruptedException {
        return queue.take(); // 如果队列空，等待直到有元素
    }
}
