package org.apache.hugegraph.traversal.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

public class FixedSizeArrayList<Edge> {

    private final int blockSize;
    private int maxSize;
    private final List<Edge[]> data;

    public FixedSizeArrayList(int blockSize,int maxSize) {
        this.blockSize = blockSize;
        this.data = new ArrayList<>();
        this.maxSize = maxSize;
    }

    public void add(Edge[] block) throws InterruptedException {
        if (block.length != blockSize) {
            throw new IllegalArgumentException("Block size must be " + blockSize);
        }
        while(data.size() >= maxSize){
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while waiting: " + e.getMessage());
            }
        }
        data.add(block);
    }

    public Edge[] get(int index) {
        if (index < 0 || index >= data.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return data.get(index);
    }

    public int size() {
        return data.size();
    }
}
