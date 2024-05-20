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



public class kneighbor extends OltpTraverser {

    private static final int BUFFER_SIZE = 100; // 数据缓冲区大小
    private static final int THREAD_COUNT = 10; // 线程池大小

    //private List<Edge> dataBuffer = new ArrayList<>(BUFFER_SIZE);

    //private List<Edge> resultBuffer = new ArrayList<>(BUFFER_SIZE);



    //ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    static BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    static List<ElementTask> taskss = new ArrayList<>();

    static class ElementTask implements Runnable {
        private final List<Edge> edges;
        private List<Edge> localResult; // 用于保存局部结果
        //private final CountDownLatch latch;

        public ElementTask(List<Edge> edges) {
            //this.latch = latch;
            this.edges = new ArrayList<>(edges);
            this.localResult = new ArrayList<>();
        }

        @Override
        public void run() {
            //System.out.println(Thread.currentThread().getId()+"run");
            //List<Edge> filteredEdges = new ArrayList<>();
            // for (Edge edge : edges) {
            //     if (filterEdge(edge)) {
            //         //filteredEdges.add(edge);
            //         localResult.add(edge);
            //     }
            // }
            try {
                for (Edge edge : edges) {
                    if (filterEdge(edge)) {
                        //filteredEdges.add(edge);
                        localResult.add(edge);
                    }
                }
            } finally {
                //latch.countDown(); // 完成任务时递减计数
            }
            // System.out.println("Thread "+Thread.currentThread().getId()+" process:");
            // for(Edge e : localResult){
            //     System.out.println(e);
            // }
            //System.out.println(edges.size());

            // 这里处理每个任务的10个元素
            // List<Edge> filteredEdges = new ArrayList<>();
            // for (Edge edge : edges) {
            //     if (filterEdge(edge)) {
            //         filteredEdges.add(edge);
            //     }
            // }
            // for(Edge edge : filteredEdges){
            //     System.out.println("Thread"+Thread.currentThread().getId()+" "+edge);
            // }

            // Edge ed = filteredEdges.get(1);
            // System.out.println(ed);

            //int startP;
            //long threadId = Thread.currentThread().getId();
            //startP = (int) threadId%10 * edges.size();
            //int endP = startP + edges.size();
            //List<Edge> mylist = threadLocalList.get();
            // if(endP > resultBuffer.size()){
            //     throw new IllegalArgumentException("插入的数据超出了resultBuffer的容量");
            // }
            // for(int i = 0 ;i <filteredEdges.size(); i++){
            //     System.out.println(filteredEdges.get(i));
            // }
            // for(Edge edge : filteredEdges){
            //     System.out.println(edge);
            // }
            // for (int i = 0; i < filteredEdges.size(); i++) {
            //     //mylist.set(i, filteredEdges.get(i));
            //     //resultBuffer.set(startP + i, filteredEdges.get(i));
            //     //System.out.println(threadId+" process: "+resultBuffer.get(startP+i));
            //     //System.out.println(filteredEdges.get(i));
            // }

            // for(Edge e : resultBuffer){
            //     System.out.println(e);
            // }
            // System.out.println(threadId+":");

            //edges.forEach(edge -> System.out.println(edge));

        }

        public List<Edge> getLocalResult() {
            return localResult;
        }
    }


    public kneighbor(HugeGraph graph) {
        super(graph);
        //  for (int i = 0; i < BUFFER_SIZE; i++) {
        //      resultBuffer.add(null); // 使用null或某种特定的空值初始化
        //  }
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


        // 初始化两个的Set, 添加初始顶点
        Set<Id> latest = newSet();
        Set<Id> all = newSet();
        latest.add(sourceV);
        all.add(sourceV);

        // 按每层遍历
        while (depth-- > 0) {
            long remaining = limit == NO_LIMIT ? NO_LIMIT : limit - all.size();
            // 每次更新latest集合加入相邻顶点(核心)
            //CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            latest = adjacentVertices(latest, dir, labelId, all, degree, remaining);
            all.addAll(latest);

            if (limit != NO_LIMIT && all.size() >= limit) break; //遍历点数超过上限则跳出
        }
        return all;
    }


    //每层处理
    private Set<Id> adjacentVertices(Set<Id> vertices, Directions dir,Id label,
                                     Set<Id> excluded, long degree, long limit) {
        if (limit == 0) return ImmutableSet.of();

        Set<Id> neighbors = newSet();
        // 依次遍历latest顶点
        //ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (Id source : vertices) {
            // 拿到从这个点出发的所有边,,时间复杂度至少O(n)?
            Iterator<Edge> edges = edgesOfVertex(source, dir,label, degree);
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            //List<Edge> dataBuffer = new ArrayList<>(BUFFER_SIZE);

           // BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
            //List<ElementTask> taskss = new ArrayList<>();


            //对边集合进行多线程过滤
            // processEdges(edges);
            filteredges(edges,executor);

            executor.shutdown(); // 关闭线程池
            try {
                executor.awaitTermination(20, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            List<Edge> aggregatedResult = new ArrayList<>();
            for (ElementTask task : taskss) {
                aggregatedResult.addAll(task.getLocalResult());
            }

            // while (edges.hasNext()) {
            //      HugeEdge e = (HugeEdge) edges.next();
            //      // 获得每条边指向的顶点ID
            //      Id target = e.id().otherVertexId();
            //      // 跳过or添加这个点
            //     if (excluded != null && excluded.contains(target)) continue;

            //     neighbors.add(target);

            //      if (limit != NO_LIMIT && neighbors.size() >= limit) return neighbors;
            // }

            for(Edge edge : aggregatedResult){
                HugeEdge e = (HugeEdge) edge;
                Id target = e.id().otherVertexId();
                // 跳过or添加这个点
                if (excluded != null && excluded.contains(target)) continue;

                neighbors.add(target);

                if (limit != NO_LIMIT && neighbors.size() >= limit) return neighbors;
            }




            //  for(int i=0 ; i<resultBuffer.size();i++){
            //      if(resultBuffer.get(i) == null){
            //          continue;
            //      }
            //      HugeEdge e = (HugeEdge) resultBuffer.get(i);
            //      // 获得每条边指向的顶点ID
            //      Id target = e.id().otherVertexId();
            //      // 跳过or添加这个点
            //      if (excluded != null && excluded.contains(target)) continue;

            //      neighbors.add(target);

            //      if (limit != NO_LIMIT && neighbors.size() >= limit) return neighbors;

            //  }
        }
        return neighbors;

    }

    public void filteredges(Iterator<Edge> edges,ExecutorService executor) {
        List<Edge> dataBuffer = new ArrayList<>(BUFFER_SIZE);
        while (edges.hasNext()) {
            dataBuffer.add(edges.next());
            // 当数据缓冲区满，分配任务给线程
            if (dataBuffer.size() == BUFFER_SIZE) {
                distributeTasksAndProcess(executor, dataBuffer);
                dataBuffer.clear(); // 清空缓冲区以用于下一批边
            }
        }

        // 处理剩余的边
        if (!dataBuffer.isEmpty()) {
            distributeTasksAndProcess(executor, dataBuffer);
        }



    }

    private void distributeTasksAndProcess(ExecutorService executor, List<Edge> edges) {
        int taskSize = edges.size() / THREAD_COUNT;
        for (int i = 0; i < THREAD_COUNT; i++) {
            int start = i * taskSize;
            int end = (i == THREAD_COUNT - 1) ? edges.size() : (start + taskSize);
            List<Edge> subList = edges.subList(start, end);
            ElementTask task = new ElementTask(subList);
            taskQueue.add(task);
            taskss.add(task);
            //executor.submit(() -> processEdges(subList));
        }

        while (!taskQueue.isEmpty()) {
            Runnable task = taskQueue.poll(); // 从队列中取出任务
            //System.out.println("  ::"+task);
            if (task != null) {
                executor.execute(task);
            }
        }
    }
    //  private void processEdges(List<Edge> edges ) {
    //      // 在这里根据线程ID对固定数量的边进行过滤处理
    //      // 示例：仅打印边信息
    //      List<Edge> filteredEdges = new ArrayList<>();
    //      for (Edge edge : edges) {
    //          if (filterEdge(edge)) {
    //              filteredEdges.add(edge);
    //          }
    //      }

    //      int startP;
    //      long threadId = Thread.currentThread().getId();
    //      startP = (int) threadId * edges.size();
    //      int endP = startP + edges.size();
    //      if(endP > resultBuffer.size()){
    //          throw new IllegalArgumentException("插入的数据超出了resultBuffer的容量");
    //      }
    //      for (int i = 0; i < filteredEdges.size(); i++) {
    //          resultBuffer.set(startP + i, filteredEdges.get(i));
    //      }

    //      edges.forEach(edge -> System.out.println(edge));
    //  }

    private static boolean filterEdge(Edge edge) {
        // 示例过滤条件：边的type类型等于给定的类型
        //Object weightObj = edge.getProperties().get("weight");
        //Object weightObj = edge.equals(properties);
        Set<String> allowtypes =newSet();
        allowtypes.add("光链路路由");
        Iterator<Property<Object>> type = edge.properties("type");
        Property<Object> p = edge.property("type");
        return p.isPresent() && Objects.equal(p.value(), "光链路路由");


        //  if(allowtypes.contains(edge.properties("type"))){
        //      return true;
        //  };
        //        if (weightObj instanceof Number) {
        //            double weight = ((Number) weightObj).doubleValue();
        //            return weight >= weightThreshold;
        //        }
        //return true;
    }


    //    Iterator<Edge> edgesOfVertex(Id source, Directions dir, Id label, long limit) {
    //        Id[] labels = {};
    //        if (label != null) labels = new Id[]{label}; //Q2:为何下面不直接传label
    //
    //        //通过"fromV + 方向 + 边label"查询边, 这里确定是一个ConditionQuery (查的细节在后面.)
    //        Query query = GraphTransaction.constructEdgesQuery(source, dir, labels);
    //        if (limit != NO_LIMIT) query.limit(limit);
    //
    //        return this.graph.edges(query);
    //    }



    //        KneighborRecords records = new KneighborRecords(true, sourceV, true);
    //
    //        Consumer<EdgeId> consumer = edgeId -> {
    //            if (this.reachLimit(limit, records.size())) {
    //                return;
    //            }
    //            records.addPath(edgeId.ownerVertexId(), edgeId.otherVertexId());
    //        };
    //
    //        while (depth-- > 0) {
    //            records.startOneLayer(true);
    //            traverseIdsByBfs(records.keys(), dir, labelId, degree, NO_LIMIT, consumer);
    //            records.finishOneLayer();
    //            if (reachLimit(limit, records.size())) {
    //                break;
    //            }
    //        }
    //
    //        this.vertexIterCounter.addAndGet(records.size());
    //
    //        return records.idsBySet(limit);




    private boolean reachLimit(long limit, int size) {
        return limit != NO_LIMIT && size >= limit;
    }
}
