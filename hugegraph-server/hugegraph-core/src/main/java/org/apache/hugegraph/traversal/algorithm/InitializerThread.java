package org.apache.hugegraph.traversal.algorithm;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.backend.store.memory.InMemoryDBTables;
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

import com.google.common.collect.ImmutableSet;

import org.apache.hugegraph.traversal.algorithm.DataBufferList;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.records.KneighborRecords;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.tinkerpop.gremlin.structure.Edge;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.concurrent.Future;
import java.util.concurrent.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.EdgeId;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.backend.query.EdgesQueryIterator;
import org.apache.hugegraph.config.CoreOptions;
import org.apache.hugegraph.iterator.FilterIterator;
import org.apache.hugegraph.iterator.MapperIterator;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.Consumers;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.CloseableIterator;

import com.google.common.base.Objects;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.xml.stream.util.EventReaderDelegate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.traversal.algorithm.steps.Steps;
import org.apache.hugegraph.type.define.Directions;
import org.apache.hugegraph.util.E;
import org.apache.tinkerpop.gremlin.structure.Edge;


import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.apache.hugegraph.traversal.algorithm.HugeTraverser.*;


public class InitializerThread extends OltpTraverser implements Runnable{
    private static final Logger logger = Logger.getLogger(InitializerThread.class.getName());

    static {
        try {
            // 创建 FileHandler，指定日志文件路径为 /path/to/log/file.log
            FileHandler fileHandler = new FileHandler("/home/llw/hugegraph/hugegraph-server/apache-hugegraph-incubating-1.5.0/logs/file-init.log");

            // 设置日志文件的格式为 SimpleFormatter
            fileHandler.setFormatter(new SimpleFormatter());

            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ExecutorService threadPool;//线程池
    public static RequestQueue requestQueue;//请求队列
    private static msgQueue msgqueue;//消息队列

    private static List<DataBuffer> dataBuffer;
    private static TempBuffer<Edge> tempBuffer;
    private static Resultbuffer resultBuffer;

    private static databuffermsg[] computeMsgQueues;

    private static DataBufferList dataBufferList;
    private static  AtomicInteger done = new AtomicInteger();//原子计数器

    private static volatile boolean producerWorking = true;

    private static int  Thread_COUNT=2;


    private Id vertexId;
    private Directions dir;
    private Id label;
    private int depth;
    private long degree;
    private long limit;



    public InitializerThread(HugeGraph graph,Id vertexId, Directions dir,Id label,int depth,long degree,long limit) {
        super(graph);
        this.vertexId = vertexId;
        this.dir = dir;
        this.label=label;
        this.depth=depth;
        this.degree=degree;
        this.limit=limit;

        logger.info("InitializerThread constructor called with parameters: " +
                "graph=" + graph + ", vertexId=" + vertexId + ", dir=" + dir +
                ", label=" + label + ", depth=" + depth + ", degree=" + degree +
                ", limit=" + limit);
    }

    static {
        logger.info("InitializerThread static initialization block started");
        // 初始化队列
        requestQueue = new RequestQueue();
        msgqueue = new msgQueue();

        //分配缓冲区
        dataBufferList = new DataBufferList();

        // 创建固定大小的线程池
        threadPool = Executors.newFixedThreadPool(2);

        // 分配缓冲区
        //dataBuffer = new DataBuffer(1000,100);
        // dataBuffer = new ArrayList<>();
        // for(int i=0;i<2;i++){
        //     dataBuffer.add(new DataBuffer(100,100));
        // }

        computeMsgQueues = new databuffermsg[2];
        for (int i = 0; i < 2; i++) {
            computeMsgQueues[i] = new databuffermsg();
        }

        tempBuffer = new TempBuffer<>(2);
        resultBuffer = new Resultbuffer();
        logger.info("InitializerThread static initialization block completed");
    }

    // 提供访问方法
    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }



    public static msgQueue getMsgQueue() {
        return msgqueue;
    }

    public static DataBufferList getDataBufferList(){return dataBufferList;}

    public static List<DataBuffer> getDataBuffer() {
        return dataBuffer;
    }

    public static TempBuffer<Edge> getTempBuffer() {
        return tempBuffer;
    }

    public static Resultbuffer getResultBuffer() {
        return resultBuffer;
    }

    public static databuffermsg[] getdatabuffermsg(){return computeMsgQueues;}

    public static AtomicInteger getDone(){
        return done;
    }

    public static  boolean getBool(){
        return producerWorking;
    }



//    public int getDepth() {
//        return depth;
//    }
//
//    public long getLimit() {
//        return limit;
//    }
//
//    public long getDegree() {
//        return degree;
//    }
//
//    public Directions getEdgeDirection() {
//        return dir;
//    }


    @Override
    public void run() {
        resetStaticVariables();
        VertexRequest initrequest = new VertexRequest(vertexId,dir,label,depth,degree,limit);
        requestQueue.enqueue(initrequest);
        try {
            msgqueue.putVertexCount(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        queryThread queryThreader = new queryThread(graph());
        computeThread computeThreader0 = new computeThread(0,dir,label,depth,degree,limit);
        computeThread computeThreader1 = new computeThread(1,dir,label,depth,degree,limit);

        // for(int i=0;i<dataBuffer.size();i++){
        //     dataBuffer.get(i).addListener(computeThreader);
        // }

        Thread workthread = new Thread(queryThreader);
        Thread computethread0 = new Thread(computeThreader0);
        Thread computethread1 = new Thread(computeThreader1);


        msgqueue.setIter1(depth);
        msgqueue.setIter2(depth);





        workthread.start();
        computethread0.start();
        computethread1.start();
        //threadPool.submit(computeThreader0);
        //threadPool.submit(computeThreader1);

        try {
            workthread.join();
            computethread0.join();
            computethread1.join();
            //threadPool.shutdown();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread was interrupted.");
        }finally{
            //resetStaticVariables();
        }


    }

    private static void resetStaticVariables() {
        threadPool.shutdownNow();  // 尝试安全关闭线程池
        requestQueue = new RequestQueue();  // 重新初始化请求队列
        msgqueue = new msgQueue();  // 重新初始化消息队列
        dataBufferList = new DataBufferList();  // 重新初始化数据缓冲区列表
        tempBuffer = new TempBuffer<>(2);  // 重新初始化临时缓冲区
        resultBuffer = new Resultbuffer();  // 重新初始化结果缓冲区
        computeMsgQueues = new databuffermsg[2];  // 重新初始化数据消息队列
        for (int i = 0; i < 2; i++) {
            computeMsgQueues[i] = new databuffermsg();
        }
        done.set(0);  // 重置完成计数器
        producerWorking = true;  // 重置生产者工作状态
    }
}
