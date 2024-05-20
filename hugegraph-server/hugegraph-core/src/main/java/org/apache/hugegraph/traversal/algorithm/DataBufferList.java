package org.apache.hugegraph.traversal.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.records.KneighborRecords;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.tinkerpop.gremlin.structure.Edge;


public class DataBufferList {
    private static List<DataBuffer> buffers;
    private static int currentIndex;
    private static DataBuffer currentBuffer;
    private static boolean queryThreadFinished;

    public DataBufferList() {
        // 初始化缓冲区列表
        buffers = new ArrayList<>();
        currentIndex = 0;
        //queryThreadFinished = false;
        // 添加初始化的 DataBuffer 实例
        for(int i=0;i<2;i++){
            buffers.add(new DataBuffer(20000,10000));
        }
        // 添加其他 DataBuffer 实例，这里可以根据需要进行添加
        // 初始化当前活动的 DataBuffer
        currentBuffer = buffers.get(currentIndex);
    }

    public void addtoBUFF(Edge edge) {
        // 如果当前 DataBuffer 满了，寻找下一个可用的 DataBuffer
        if (!currentBuffer.compareAndCheckZero()) {
            //currentBuffer.clear();
            currentBuffer = findNextDatabuffer();
            //currentBuffer.clear();
        }

        currentBuffer.add(edge);
        // 将数据写入到当前 DataBuffer 中
        //writeToDataBuffer(currentBuffer, edge);
    }

    public DataBuffer getCurrentBuffer(){
        return currentBuffer;
    }

    public static boolean isQueryThreadFinished() {
        return queryThreadFinished;
    }

    public static void setQueryThreadFinished(boolean finished) {
        queryThreadFinished = finished;
    }

    public int getCurrentIndex(){return currentIndex;}

    private  DataBuffer findNextDatabuffer() {
        // 在数据缓冲区列表中查找下一个可用的 DataBuffer
        DataBuffer buffer = buffers.get(currentIndex);
        // 循环查找可用的 DataBuffer，直到找到一个可以写入的或者已经遍历了所有的 DataBuffer
        int startIdx = currentIndex;
        do {
            // 移动到下一个 DataBuffer
            currentIndex++;
            if (currentIndex >= buffers.size()) {
                currentIndex = 0;
            }
            // 如果已经遍历了一轮，则表示所有 DataBuffer 都已满，退出循环
            if (currentIndex == startIdx) {
                break;
            }
            buffer = buffers.get(currentIndex);
        } while (!buffer.compareAndCheckZero());
        return buffer;
    }

    private  void writeToDataBuffer(DataBuffer buffer, Edge edge) {
        //buffer.addToDatabuffer();
        buffer.add(edge);

        // 将数据写入到指定的 DataBuffer 中
        // ...
    }
}
