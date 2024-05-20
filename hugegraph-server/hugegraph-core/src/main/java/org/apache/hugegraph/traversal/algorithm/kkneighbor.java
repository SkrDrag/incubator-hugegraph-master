/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hugegraph.traversal.algorithm;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
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

public class kkneighbor extends OltpTraverser {
    private static final Logger logger = Logger.getLogger(kkneighbor.class.getName());
    static {
        try {
            // 创建 FileHandler，指定日志文件路径为 /path/to/log/file.log
            FileHandler fileHandler = new FileHandler("/home/llw/hugegraph/hugegraph-server/apache-hugegraph-incubating-1.5.0/logs/file-3.log");

            // 设置日志文件的格式为 SimpleFormatter
            fileHandler.setFormatter(new SimpleFormatter());

            // 将 FileHandler 添加到 logger 中
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final int THREAD_COUNT = 4;
    private static final int BUFFER_SIZE = 100;
    private static final long NO_LIMIT = -1;

    private final ExecutorService producerExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService consumerExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
    private final ExecutorService collectorExecutor = Executors.newSingleThreadExecutor();
    //private final BlockingQueue<Edge> edgeBuffer = new LinkedBlockingQueue<>();
    private ConcurrentLinkedQueue<Edge> edgeBuffer = new ConcurrentLinkedQueue<>();
    private volatile boolean producerWorking = true;

    public kkneighbor(HugeGraph graph) {
        super(graph);
    }

    public Set<Id> kneighbor(Id sourceV, Directions dir, String label, int depth,
                             long degree, long limit) {
        E.checkNotNull(sourceV, "source vertex id");
        this.checkVertexExist(sourceV, "source vertex");
        E.checkNotNull(dir, "direction");
        checkPositive(depth, "k-neighbor max_depth");
        checkDegree(degree);
        checkLimit(limit);

        Id labelId = getEdgeLabelId(label);

        logger.info("kneighbor start with" + depth);

        Set<Id> latest = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Set<Id> all = Collections.newSetFromMap(new ConcurrentHashMap<>());
        latest.add(sourceV);
        all.add(sourceV);

        for (int i = 1; i <= depth; i++) {
            producerWorking = true;
            long remaining = (limit == NO_LIMIT) ? NO_LIMIT : limit - all.size();
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT+1);
            adjacentVertices(latest, dir, labelId, all, degree, remaining, latch);
            int size= all.size();
            String message = "All data in layer " + i + " data size:"+size+": " + all.toString();
            logger.info(message);
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.warning("Interrupted while waiting for latch countdown.");
                Thread.currentThread().interrupt();
            }
        }
        int size = all.size();

        String mes = "Result  :"+"size: " +size+"  :"+ all.toString();

        logger.info(mes);

        shutdownExecutors();

        return all;
    }

    private void adjacentVertices(Set<Id> vertices, Directions dir, Id label,
                                  Set<Id> excluded, long degree, long limit, CountDownLatch latch) {
        producerExecutor.execute(() -> produce(vertices, dir, label, excluded, degree, limit, latch));
        for (int i = 0; i < THREAD_COUNT; i++) {
            consumerExecutor.execute(() -> consume(vertices, excluded, limit, latch));
        }
    }

    private void produce(Set<Id> vertices, Directions dir, Id labelId, Set<Id> excluded, long degree, long limit, CountDownLatch latch) {
        try {
            long threadId = Thread.currentThread().getId();
            for (Id source : vertices) {
                Iterator<Edge> edges = edgesOfVertex(source, dir, labelId, degree);
                while (edges.hasNext()) {
                    Edge edge = edges.next();
                    edgeBuffer.offer(edge);
                }
            }
            latch.countDown();
            producerWorking = false;
            logger.info("Produced thread" + threadId +"  produce "+ edgeBuffer.size() + " edges");
        } catch (Exception e) {
            logger.warning("Error during edge production: " + e.getMessage());
            latch.countDown();
        }
    }

    private void consume(Set<Id> latest, Set<Id> all, long limit, CountDownLatch latch) {
        try {
            long threadId = Thread.currentThread().getId();
            Set<Id> neighbors = new HashSet<>();
            while (!edgeBuffer.isEmpty() || producerWorking) {
                Edge edge = edgeBuffer.poll();
                if (edge != null) {
                    HugeEdge e = (HugeEdge) edge;
                    Id target = e.id().otherVertexId();
                    if (!all.contains(target)) {
                        neighbors.add(target);
                        if (limit != NO_LIMIT && neighbors.size() >= limit) {
                            break;
                        }
                    }
                } else if (edgeBuffer.isEmpty()) {
                    if(!producerWorking){
                        break;
                    }
                }

            }
            latest.addAll(neighbors);
            all.addAll(neighbors);
            latch.countDown();
            logger.info("Consumed thread" + threadId +" filter  "+ neighbors.size() + " neighbors");
        } catch (Exception e) {
            logger.warning("Error during edge consumption: " + e.getMessage());
            latch.countDown();
        }
    }

    private void shutdownExecutors() {
        try {
            producerExecutor.shutdown();
            consumerExecutor.shutdown();
            collectorExecutor.shutdown();
            if (!producerExecutor.awaitTermination(30, TimeUnit.SECONDS) ||
                    !consumerExecutor.awaitTermination(30, TimeUnit.SECONDS) ||
                    !collectorExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("One or more executors did not terminate in the allotted time.");
            }
        } catch (InterruptedException e) {
            logger.warning("Interrupted while waiting for executors termination.");
            Thread.currentThread().interrupt();
        }
    }
}



