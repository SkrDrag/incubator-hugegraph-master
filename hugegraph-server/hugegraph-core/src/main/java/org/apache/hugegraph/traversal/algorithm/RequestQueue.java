package org.apache.hugegraph.traversal.algorithm;

import java.util.LinkedList;
import java.util.Queue;

public class RequestQueue {
	    private Queue<VertexRequest> queue;

          public RequestQueue() {
        	        this.queue = new LinkedList<>();
           }

        	    // 将顶点请求添加到队列中
        	    public void enqueue(VertexRequest request) {
                queue.offer(request);
        	    }

        	    // 从队列中移除并返回顶点请求
            public VertexRequest dequeue() {
        	        return queue.poll();
            }
 	    // 检查队列是否为空
        	    public boolean isEmpty() {
        	        return queue.isEmpty();
            }
            	    // 获取队列的大小
        	    public int size() {
        	        return queue.size();
        	    }
	}

