package org.apache.hugegraph.traversal.algorithm;

import org.apache.hugegraph.backend.query.Query;
import org.apache.hugegraph.backend.store.memory.InMemoryDBTables;
import org.apache.hugegraph.backend.tx.GraphTransaction;
import org.apache.hugegraph.traversal.algorithm.InitializerThread;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;

import org.apache.hugegraph.traversal.algorithm.DataBufferList;

import org.apache.hugegraph.traversal.algorithm.databuffermsg;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.records.KneighborRecords;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.tinkerpop.gremlin.structure.Edge;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.records.KneighborRecords;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.traversal.optimize.TraversalUtil;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
public class queryThread extends OltpTraverser implements Runnable {
    private static final Logger logger = Logger.getLogger(queryThread.class.getName());

    static {
        try {
            // 创建 FileHandler，指定日志文件路径为 /path/to/log/file.log
            FileHandler fileHandler = new FileHandler("/home/llw/hugegraph/hugegraph-server/apache-hugegraph-incubating-1.5.0/logs/file-queryThread.log");

            // 设置日志文件的格式为 SimpleFormatter
            fileHandler.setFormatter(new SimpleFormatter());

            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    RequestQueue queue = InitializerThread.getRequestQueue();
    //List<DataBuffer> dataBuffer = InitializerThread.getDataBuffer();
    msgQueue msgqueue = InitializerThread.getMsgQueue();

    DataBufferList dataBufferList = InitializerThread.getDataBufferList();

    TempBuffer<Edge> tempBuffer = InitializerThread.getTempBuffer();

    private  volatile boolean producewoking = InitializerThread.getBool();

    Resultbuffer resultbuffer = InitializerThread.getResultBuffer();

    databuffermsg datamsg[] = InitializerThread.getdatabuffermsg();

    //private boolean isLastBufferFilled = false;




    public queryThread(HugeGraph graph) {
        super(graph);
    }

    @Override
    public void run() {

        while(msgqueue.getIter1()>0){
            try {
                // if(msgqueue.getIter()<=0){
                //     logger.info("over!!!");
                //     break;
                // }

                logger.info("depth: "+String.valueOf(msgqueue.getIter1()));
                int count = msgqueue.takeVertexCount();
                logger.info(String.valueOf(count));
                // for(int i=0;i<count;i++){
                //     VertexRequest request = queue.dequeue();
                //     logger.info(request.toString());
                //     processRequest(request);
                // }
                processRequestque(queue,count);
                DataBufferList.setQueryThreadFinished(true);
                logger.info("work:finish"+DataBufferList.isQueryThreadFinished());
                msgqueue.decrementIter1();
                //producewoking=false;
                //logger.info("produceworking:"+producewoking);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        logger.info("depth: "+String.valueOf(msgqueue.getIter1()));
        logger.info("query ending");

    }

    private void processRequestque(RequestQueue queue,int count){
        logger.info("process start!!!");
        for(int i=0;i<count;i++){
            VertexRequest request = queue.dequeue();
            logger.info(request.toString());
            Iterator<Edge> edges = edgesOfVertex(request.getVertexId(), request.getEdgeDirection(), request.getLabel(), request.getDegree());
            while(edges.hasNext()){
                //将数据写入可用的databuffer
                Edge edge = edges.next();
                //logger.info("edge: "+edge.toString());
                dataBufferList.addtoBUFF(edge);
                DataBuffer buffer = dataBufferList.getCurrentBuffer();
                int current = dataBufferList.getCurrentIndex();
                //logger.info("current buffer: "+current);
                logger.info("blockid:"+buffer.getblockindex()+"index:"+buffer.getindex());
                logger.info("blocksize:"+buffer.getBlockSize());
                if(buffer.getblockindex()>=buffer.getNumBlocks() || (!edges.hasNext() && i==count-1)){
                    logger.info("current buffer is full :"+ current);

                    DataBuffer fullbuffer = dataBufferList.getCurrentBuffer();
                    fullbuffer.getCount().set(fullbuffer.getNumBlocks());
                    logger.info("COUNT:"+dataBufferList.getCurrentBuffer().getCount());
                    //通知多个计算线程
                    for(int j=0 ;j<2 ;j++){
                        datamsg[j].enqueue(fullbuffer);
                    }
                    //datamsg[0].enqueue(fullbuffer);
                    //datamsg[1].enqueue(fullbuffer);
                }
            }

        }

    }


//     private  void processRequest(VertexRequest request) throws InterruptedException {
//         Iterator<Edge> edges = edgesOfVertex(request.getVertexId(), request.getEdgeDirection(), request.getLabel(), request.getDegree());
//         logger.info(edges.next().toString());
// //        int index=0;
// //        int blockid=0;
//         while(edges.hasNext()){
//             DataBuffer buffer = findNextDatabuffer();
//             // if(buffer == null){
//             //     logger.info("buffer is full");
//             //     //wait();
//             // }
//             int index=0;
//             int blockid=0;
//             logger.info("Numblocks:"+buffer.getNumBlocks()+" Blocksize:"+buffer.getBlockSize());
//             while(blockid<buffer.getNumBlocks()){
//                 if(index>=buffer.getBlockSize()){
//                     index=0;
//                     blockid++;
//                 }
//                 if(!edges.hasNext()){
//                     logger.info("final");
//                     //buffer.setWork(0);
//                     //logger.info("work:"+dabuffer.getwork());
//                     break;
//                 }
//                  Edge edge = edges.next();
//                 // HugeEdge e = (HugeEdge) edge;
//                 // Id target = e.id().otherVertexId();
//                 //logger.info("ID:"+target);
//                 //resultbuffer.addNode(target);
//                 //logger.info("index "+index+" edges: "+edge.toString());
//                 buffer.addToDatabuffer(blockid,index,edge);
//                 // String msg = buffer.getBlock(blockid).getEdges().toString();
//                 //logger.info("edegs"+msg);
//                 index++;
//             }
//             // Edge[] edgess = buffer.getBlock(0).getEdges();
//             // for(Edge edge1 :edgess){
//             //     logger.info(edge1.toString());
//             // }
//             buffer.getCount().set(buffer.getNumBlocks());
//             logger.info("count:"+buffer.getCount().toString());
//             //dataBuffer.get(1).notifyListeners();
//             // buffer.notifyListeners();
//             datamsg[0].enqueue(buffer);
//             //logger.info("msgqueue:" + datamsg[0].toString());

//         }

//     }

    // private DataBuffer findNextDatabuffer(){
    //     for(int i=0;i<dataBuffer.size();i++){
    //         logger.info("databuffer count: "+dataBuffer.get(i).getCount().toString());
    //         if(dataBuffer.get(i).compareAndCheckZero()){
    //             return dataBuffer.get(i);
    //         }
    //     }
    //     return null;
    // }

    // private DataBuffer findNextDatabuffer() {
    //     DataBuffer nextBuffer = null;
    //     while (nextBuffer == null) {
    //         for (int i = 0; i < dataBuffer.size(); i++) {
    //             if (dataBuffer.get(i).compareAndCheckZero()) {
    //                 nextBuffer = dataBuffer.get(i);
    //                 break;
    //             }
    //         }
    //         if (nextBuffer == null) {
    //             // 如果没有找到可用的 DataBuffer，则线程休眠一段时间再重新尝试
    //             try {
    //                 logger.info("buffer is full");
    //                 Thread.sleep(1000); // 休眠 1 秒
    //             } catch (InterruptedException e) {
    //                 Thread.currentThread().interrupt();
    //                 logger.warning("Thread interrupted while sleeping.");
    //             }
    //         }
    //     }
    //     return nextBuffer;
    // }


//    private void fillLastBuffer(DataBuffer buffer, int blockId, int index) {
//        while (index < buffer.getBlockSize() && buffer.getCount().get() < buffer.getMaxCount()) {
//            if (index == buffer.getBlockSize() - 1) {
//                // Last block of the last buffer, fill with a special marker
//                buffer.addToDatabuffer(blockId, index, SpecialMarker.LAST_MARKER);
//            } else {
//                buffer.addToDatabuffer(blockId, index, edges.next());
//            }
//            index++;
//            buffer.getCount().incrementAndGet();
//        }
//        // Set the flag indicating the last buffer is filled
//        isLastBufferFilled = true;
//        // Notify listeners
//        buffer.notifyListeners();
//    }

    private enum SpecialMarker {
        LAST_MARKER
    }


}
