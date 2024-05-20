package org.apache.hugegraph.traversal.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
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
import org.checkerframework.common.value.qual.EnsuresMinLenIf;

public class Resultbuffer {
    Set<Id> neighbors;
    List<List<Id>> path;

    List<Edge> edges;

    public Resultbuffer() {
        this.neighbors = new HashSet<>();
        this.path = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void addNode(Id neighbor){
        neighbors.add(neighbor);
    }

    public void addNodes(Set<Id> neigh){
        neighbors.addAll(neigh);
    }

    public void addEdges(List<Edge> edge){
        edges.addAll(edge);
    }


}
