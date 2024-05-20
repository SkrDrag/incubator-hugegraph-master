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
import org.apache.hugegraph.traversal.algorithm.kneighbor;
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
import java.util.HashMap;
import java.util.Map;
public class KneighborManager {
    private Map<String, kneighbor> instances;

    public KneighborManager() {
        this.instances = new HashMap<>();
    }

    // 添加实例到管理器
    public void addInstance(String instanceName, HugeGraph graph) {
        //kneighbor instance = new kneighbor(graph);
        kneighbor instance = new kneighbor(graph);
        this.instances.put(instanceName, instance);
    }

    // 根据实例名称获取实例
    public kneighbor getInstance(String instanceName) {
        return this.instances.get(instanceName);
    }

    // 移除实例
    public void removeInstance(String instanceName) {
        this.instances.remove(instanceName);
    }

    // 获取所有实例名称
    public Set<String> getAllInstanceNames() {
        return this.instances.keySet();
    }
}
