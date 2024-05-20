package org.apache.hugegraph.traversal.algorithm;

import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.structure.HugeEdge;
import org.apache.hugegraph.type.define.Directions;
import org.apache.tinkerpop.gremlin.structure.Edge;

import org.apache.hugegraph.traversal.algorithm.databuffermsg;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class computeThread implements DataBufferListener, Runnable{

    private static final Logger logger = Logger.getLogger(computeThread.class.getName());

    static {
        try {
            // 创建 FileHandler，指定日志文件路径为 /path/to/log/file.log
            FileHandler fileHandler = new FileHandler("/home/llw/hugegraph/hugegraph-server/apache-hugegraph-incubating-1.5.0/logs/file-comThread.log");

            // 设置日志文件的格式为 SimpleFormatter
            fileHandler.setFormatter(new SimpleFormatter());

            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Directions dir;
    private Id label;
    private int depth;
    private long degree;
    private long limit;

    private int index;
    TempBuffer<Edge> tempBuffer = InitializerThread.getTempBuffer();
    Resultbuffer resultBuffer = InitializerThread.getResultBuffer();
    RequestQueue queue = InitializerThread.getRequestQueue();

    databuffermsg databuffermsg[] = InitializerThread.getdatabuffermsg();
    msgQueue msgqueue = InitializerThread.getMsgQueue();

    //private  volatile boolean producewoking = InitializerThread.getBool();


    public computeThread(int index,Directions dir,Id label,int depth,long degree,long limit){
        this.index=index;
        this.dir = dir;
        this.label=label;
        this.depth=depth;
        this.degree=degree;
        this.limit=limit;

    }
    @Override
    public void run() {
        while(msgqueue.getIter2()>0){
            logger.info("depth: "+String.valueOf(msgqueue.getIter2()));
            DataBuffer dabuffer = databuffermsg[index].dequeue();
            logger.info("msgSize:"+databuffermsg[index].getDatasize());
            String msg = "Received data buffer full event for buffer: " + dabuffer.getBlock(index).getSize();
            logger.info(msg);
            GetDatabuffer(dabuffer,index);
            //String msg = "com databuffer"+dabuffer.toString();
            //logger.info(msg);
            //logger.info("work:"+dabuffer.getwork());
            logger.info("work:"+DataBufferList.isQueryThreadFinished());
            if(DataBufferList.isQueryThreadFinished() && databuffermsg[index].getDatasize()==0){
                logger.info("final datablock");
                int done = InitializerThread.getDone().incrementAndGet();
                if(done==2){//Done计数器等于线程总数
                    synchronized (InitializerThread.class){
                        if(InitializerThread.getDone().get()==done){
                            logger.info("thread:"+index+" agg start!!!!!!!");
                            List<Edge>[] temp = tempBuffer.getBuffer();
                            for(int i=0;i<temp.length;i++){
                                for(Edge edge : temp[i]){
                                    HugeEdge e = (HugeEdge) edge;
                                    Set<Id> s=e.getPropertyKeys();
                                    logger.info("key:"+s);
                                    for(Id id:s){
                                        logger.info("property:"+e.getPropertyValue(id));
                                    }
                                    Id target = e.id().otherVertexId();
                                    logger.info("ID: "+i+" "+target);
                                    if (resultBuffer.neighbors != null && resultBuffer.neighbors.contains(target)) continue;
                                    tempBuffer.addId(target);

                                }
                            }
                            resultBuffer.addNodes(tempBuffer.getNeighbors());
                            //resultBuffer.addEdges(tempBuffer.getList(index));
                            //depth--;
                            msgqueue.decrementIter2();
                            if(msgqueue.getIter2()<=0){
                                break;
                            }
                            //请求入队
                            int t=0;
                            for(Id id : tempBuffer.getNeighbors()){
                                VertexRequest request = new VertexRequest(id,dir,label,depth,degree,limit);
                                queue.enqueue(request);
                                t++;
                            }

                            try {
                                //msgqueue.decrementIter2();
                                //logger.info("depth: "+String.valueOf(msgqueue.getIter2()));
                                InitializerThread.getDone().set(0);
                                DataBufferList.setQueryThreadFinished(false);
                                dabuffer.clear();
                                tempBuffer.clear();
                                logger.info("request size :"+t);
                                msgqueue.putVertexCount(t);


                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }



                    }
                }

            }

        }
        logger.info("depth: "+String.valueOf(msgqueue.getIter2()));
        logger.info("compute ending");
    }

    @Override
    public void onDataBufferFull(DataBufferEvent event) {
        DataBuffer dabuffer = event.getDataBuffer();

        // if(InitializerThread.getDone().incrementAndGet()==1){//done加1后等于线程总数说明时最后一个完成任务的
        //     logger.info("thread"+index+"agg start");
        //     List<Edge>[] temp = tempBuffer.getBuffer();
        //     for(int i=0;i<temp.length;i++){
        //         for(Edge edge : temp[i]){
        //             HugeEdge e = (HugeEdge) edge;
        //             Id target = e.id().otherVertexId();
        //             logger.info("ID: "+i+" "+target);
        //             if (resultBuffer.neighbors != null && resultBuffer.neighbors.contains(target)) continue;
        //             tempBuffer.addId(target);

        //         }
        //     }
        //     resultBuffer.addNodes(tempBuffer.getNeighbors());
        //     //resultBuffer.addEdges(tempBuffer.getList(index));
        //     //depth--;
        //     msgqueue.decrementIter2();
        //     if(msgqueue.getIter2()<=0){
        //         break;
        //     }
        //     //请求入队
        //     int t=0;
        //     for(Id id : tempBuffer.getNeighbors()){
        //         VertexRequest request = new VertexRequest(id,dir,label,depth,degree,limit);
        //         queue.enqueue(request);
        //         t++;
        //     }

        //     try {
        //         //msgqueue.decrementIter2();
        //         //logger.info("depth: "+String.valueOf(msgqueue.getIter2()));
        //         InitializerThread.getDone().set(0);
        //         DataBufferList.setQueryThreadFinished(false);
        //         dabuffer.clear();
        //         tempBuffer.clear();
        //         logger.info("request size :"+t);
        //         msgqueue.putVertexCount(t);


        //     } catch (InterruptedException e) {
        //         throw new RuntimeException(e);
        //     }

        // }
        // else{



        // }



    }

    private void GetDatabuffer(DataBuffer dataBuffer,int index){
        //logger.info("start compute");
        logger.info("thread:"+index+"start compute block"+index);
        DataBlock block  = dataBuffer.getBlock(index);
        if ( block.getEdges() == null || block.getSize() == 0) {
            logger.info("Data block is empty or null for thread: " + index);
            dataBuffer.getCount().decrementAndGet();
            logger.info("Remaining count after processing:"+ dataBuffer.getCount());
            return; // 数据块为空时直接返回
        }
        Edge[] edges = block.getEdges();
        //logger.info("blocksize: "+block.getSize());
        for(int i=0;i<block.getSize();i++){
            //filter edge
            // HugeEdge e = (HugeEdge)edges[i];
            // Id target = e.id().otherVertexId();

            //resultBuffer.addNode(target);

            //tempBuffer.addId(target);
            tempBuffer.addToBuffer(index,edges[i]);
        }
        //logger.info("work 1:"+dataBuffer.getCount());
        dataBuffer.getCount().decrementAndGet();
        logger.info("Remaining count after processing:"+ dataBuffer.getCount());
        //logger.info("work 1:"+dataBuffer.getCount());

    }

    //private void requestenqueue()
}
