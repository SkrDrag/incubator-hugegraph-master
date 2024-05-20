package org.apache.hugegraph.traversal.algorithm;

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
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;


public class graphcompute extends OltpTraverser {
    private static final Logger logger = Logger.getLogger(graphcompute.class.getName());
    static {
        try {
            // 创建 FileHandler，指定日志文件路径为 /path/to/log/file.log
            FileHandler fileHandler = new FileHandler("/home/llw/hugegraph/hugegraph-server/apache-hugegraph-incubating-1.5.0/logs/file-com.log");

            // 设置日志文件的格式为 SimpleFormatter
            fileHandler.setFormatter(new SimpleFormatter());

            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public graphcompute(HugeGraph graph) {
        super(graph);
    }

    public Set<Id> kneighbor(Id sourceV, Directions dir,
                             String label, int depth,
                             long degree, long limit) {
        E.checkNotNull(sourceV, "source vertex id");
        this.checkVertexExist(sourceV, "source vertex");
        E.checkNotNull(dir, "direction");
        checkPositive(depth, "k-neighbor max_depth");
        checkDegree(degree);
        checkLimit(limit);


        Id labelId = this.getEdgeLabelId(label);
        InitializerThread initializer = null;
        try {
            initializer = new InitializerThread(graph(), sourceV, dir, labelId, depth, degree, limit);
        } catch (Exception e) {
            logger.warning((Supplier<String>) e);
            e.printStackTrace();
        }

        //InitializerThread initializer = new InitializerThread(graph(),sourceV,dir,labelId,depth,degree,limit);
//        queryThread queryThreader = new queryThread(graph());
//        computeThread computeThreader = new computeThread(1,dir,labelId,depth,degree,limit);
//
        Thread thread = new Thread(initializer);
//        Thread workthread = new Thread(queryThreader);
//
        thread.start();
//        workthread.start();

        try {
            thread.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread was interrupted.");
        }

        String mes = "Result  :"+"size: " +"  :"+ InitializerThread.getResultBuffer().neighbors.size();

        logger.info(mes);

        return InitializerThread.getResultBuffer().neighbors;

    }


}
